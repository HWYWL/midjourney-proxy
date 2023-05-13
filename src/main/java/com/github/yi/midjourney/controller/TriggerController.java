package com.github.yi.midjourney.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
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
import com.github.yi.midjourney.util.Message;
import com.github.yi.midjourney.util.UVData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Message<Void> result;
        if (Action.IMAGINE.equals(submitDTO.getAction())) {
            String prompt = submitDTO.getPrompt();
            if (CharSequenceUtil.isBlank(prompt)) {
                return Message.validationError();
            }
            task = taskBuilder.prompt(prompt)
                    .finalPrompt("[" + taskId + "]" + prompt)
                    .description("/imagine " + submitDTO.getPrompt()).build();

            // 更新数据库
            taskLogService.saveOrUpdateTask(task);
            result = this.discordService.imagine(task.getFinalPrompt());
        } else {
            if (CharSequenceUtil.isBlank(submitDTO.getTaskId())) {
                return Message.validationError();
            }
            Task targetTask =taskLogService.getOneByTaskId(submitDTO.getTaskId());
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
                result = this.discordService.upscale(targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
            } else if (Action.VARIATION.equals(submitDTO.getAction())) {
                taskBuilder.description("/up " + submitDTO.getTaskId() + " V" + submitDTO.getIndex());
                result = this.discordService.variation(targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
            } else {
                // todo 暂不支持 reset, 接收mj消息时, 无法找到对应task
                return Message.of(Message.VALIDATION_ERROR_CODE, "暂不支持 reset 操作");
            }

            task = taskBuilder.build();
            taskLogService.saveOrUpdateTask(task);
        }

        if (result.getCode() != ResultEnum.SUCCESS.getCode()) {
            return Message.of(result.getCode(), result.getDescription());
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
