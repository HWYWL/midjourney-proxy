package com.github.yi.midjourney.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.github.yi.midjourney.ProxyProperties;
import com.github.yi.midjourney.configuration.Constant;
import com.github.yi.midjourney.dto.NotifyDTO;
import com.github.yi.midjourney.dto.SubmitDTO;
import com.github.yi.midjourney.dto.UVSubmitDTO;
import com.github.yi.midjourney.model.Action;
import com.github.yi.midjourney.model.ResultEnum;
import com.github.yi.midjourney.model.Task;
import com.github.yi.midjourney.model.TaskStatus;
import com.github.yi.midjourney.service.DiscordService;
import com.github.yi.midjourney.service.TaskLogService;
import com.github.yi.midjourney.util.ConvertUtils;
import com.github.yi.midjourney.util.CosUtil;
import com.github.yi.midjourney.util.Message;
import com.github.yi.midjourney.util.UVData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.TimeZone;

/**
 * 用于图像处理业务提交
 *
 * @author YI
 */
@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
public class TriggerController {
    private final DiscordService discordService;
    private final ProxyProperties properties;
    private final TaskLogService taskLogService;
    private final CosUtil cosUtil;

    @SaCheckRole(Constant.USER_ACCESS_AUTHORITY_VIP)
    @PostMapping("/submit")
    public Message<Task> submit(@RequestBody SubmitDTO submitDTO) {
        if (submitDTO.getAction() == null) {
            return Message.validationError();
        }
        if ((submitDTO.getAction() == Action.UPSCALE || submitDTO.getAction() == Action.VARIATION)
                && (submitDTO.getIndex() < 1 || submitDTO.getIndex() > 4)) {
            return Message.validationError();
        }

        String taskId = RandomUtil.randomNumbers(16);
        Task.TaskBuilder taskBuilder = Task.builder().notifyHook(submitDTO.getNotifyHook() == null ? this.properties.getNotifyHook() : submitDTO.getNotifyHook())
                .taskId(taskId)
                .submitTime(DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("Asia/Shanghai")).toTimestamp())
                .userId(StpUtil.getLoginIdAsInt())
                .action(String.valueOf(submitDTO.getAction()));

        Task task;
        if (Action.IMAGINE.equals(submitDTO.getAction())) {
            String prompt = submitDTO.getPrompt();
            if (CharSequenceUtil.isBlank(prompt)) {
                return Message.validationError();
            }

            // 拼接请求的生成图片的参数
            StringBuilder builder = new StringBuilder();
            builder.append("/imagine ");
            if (StrUtil.isNotBlank(submitDTO.getImageUrl())) {
                builder.append(StrUtil.SPACE).append(submitDTO.getImageUrl());
            }
            builder.append(StrUtil.SPACE).append(submitDTO.getPrompt()).append(StrUtil.SPACE);

            if (StrUtil.isNotBlank(submitDTO.getExtraParam())) {
                builder.append(StrUtil.SPACE).append(submitDTO.getExtraParam());
            }

            task = taskBuilder.prompt(prompt)
                    .finalPrompt("[" + taskId + "]" + prompt)
                    .description(builder.toString()).build();

            // 更新数据库
            taskLogService.saveOrUpdateTask(task);
            this.discordService.imagine(taskId, task.getFinalPrompt());
        } else {
            if (CharSequenceUtil.isBlank(submitDTO.getTaskId())) {
                return Message.validationError();
            }
            Task targetTask = taskLogService.getOneByTaskId(submitDTO.getTaskId());
            if (targetTask == null) {
                return Message.of(Message.VALIDATION_ERROR_CODE, "任务不存在或已失效");
            }
            if (!String.valueOf(TaskStatus.SUCCESS).equals(targetTask.getTaskStatus())) {
                return Message.of(Message.VALIDATION_ERROR_CODE, "关联任务状态错误");
            }

            taskBuilder.prompt(targetTask.getPrompt())
                    .finalPrompt(targetTask.getFinalPrompt())
                    .relatedTaskId(ConvertUtils.findTaskIdByFinalPrompt(targetTask.getFinalPrompt())).build();

            if (Action.UPSCALE.equals(submitDTO.getAction())) {
                taskBuilder.description("/up " + submitDTO.getTaskId() + " U" + submitDTO.getIndex());
                this.discordService.upscale(taskId, targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
            } else if (Action.VARIATION.equals(submitDTO.getAction())) {
                taskBuilder.description("/up " + submitDTO.getTaskId() + " V" + submitDTO.getIndex());
                this.discordService.variation(taskId, targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
            } else {
                // todo 暂不支持 reset, 接收mj消息时, 无法找到对应task
                return Message.of(Message.VALIDATION_ERROR_CODE, "暂不支持 reset 操作");
            }

            task = taskBuilder.build();
            taskLogService.saveOrUpdateTask(task);
        }

        return Message.success(task);
    }

    @SaCheckRole(Constant.USER_ACCESS_AUTHORITY_VIP)
    @PostMapping("/submit-uv")
    public Message<Task> submitUV(@RequestBody UVSubmitDTO uvSubmitDTO) {
        UVData uvData = ConvertUtils.convertUVData(uvSubmitDTO.getContent());
        if (uvData == null) {
            return Message.of(Message.VALIDATION_ERROR_CODE, "/up 参数错误");
        }
        SubmitDTO submitDTO = new SubmitDTO();
        submitDTO.setAction(uvData.getAction());
        submitDTO.setTaskId(uvData.getId());
        submitDTO.setIndex(uvData.getIndex());
        submitDTO.setNotifyHook(uvSubmitDTO.getNotifyHook());
        return submit(submitDTO);
    }

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 返回文件url
     */
    @SaCheckRole(Constant.USER_ACCESS_AUTHORITY_VIP)
    @RequestMapping("/upload")
    public Message<String> singleFileUpload(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        long count = Constant.IMAGE_TYPES.stream().filter(e -> filename.endsWith(e)).count();
        if (count == 0) {
            Message.of(ResultEnum.UNSUPPORTED_FILE_TYPE.getCode(), ResultEnum.UNSUPPORTED_FILE_TYPE.getMessage());
        }

        try {
            String[] split = filename.split("\\.");
            String imageType = split[split.length - 1];
            return Message.success(cosUtil.cosUpload(file, imageType));
        } catch (IOException e) {
            return Message.of(ResultEnum.FILE_UPLOAD_EXCEPTION.getCode(), ResultEnum.FILE_UPLOAD_EXCEPTION.getMessage());
        }
    }

    /**
     * 通知业务
     *
     * @return
     */
    @PostMapping("/notify")
    public Message<NotifyDTO> notifyTask(@RequestBody NotifyDTO notifyDTO) {
        System.out.println(notifyDTO);
        return Message.success(notifyDTO);
    }
}
