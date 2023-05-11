package com.github.yi.midjourney.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.yi.midjourney.ProxyProperties;
import com.github.yi.midjourney.configuration.Constant;
import com.github.yi.midjourney.dto.NotifyDTO;
import com.github.yi.midjourney.dto.SubmitDTO;
import com.github.yi.midjourney.dto.UVSubmitDTO;
import com.github.yi.midjourney.model.Action;
import com.github.yi.midjourney.model.ResultEnum;
import com.github.yi.midjourney.model.TaskStatus;
import com.github.yi.midjourney.util.Message;
import com.github.yi.midjourney.service.DiscordService;
import com.github.yi.midjourney.service.TranslateService;
import com.github.yi.midjourney.support.task.Task;
import com.github.yi.midjourney.support.task.TaskHelper;
import com.github.yi.midjourney.util.ConvertUtils;
import com.github.yi.midjourney.util.UVData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于图像处理业务提交
 */
@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
public class TriggerController {
    private final DiscordService discordService;
    private final TranslateService translateService;
    private final TaskHelper taskHelper;
    private final ProxyProperties properties;

    @SaCheckRole(Constant.USER_ACCESS_AUTHORITY_VIP)
    @PostMapping("/submit")
    public Message<Task> submit(@RequestBody SubmitDTO submitDTO) {
        if (submitDTO.getAction() == null || submitDTO.getState() == null) {
            return Message.validationError();
        }
        if ((submitDTO.getAction() == Action.UPSCALE || submitDTO.getAction() == Action.VARIATION)
                && (submitDTO.getIndex() < 1 || submitDTO.getIndex() > 4)) {
            return Message.validationError();
        }
        Task task = new Task();
        task.setNotifyHook(submitDTO.getNotifyHook() == null ? this.properties.getNotifyHook() : submitDTO.getNotifyHook());
        task.setId(RandomUtil.randomNumbers(16));
        task.setSubmitTime(System.currentTimeMillis());
        task.setState(submitDTO.getState());
        task.setAction(submitDTO.getAction());
        String key;
        Message<Void> result;
        int userId = submitDTO.getState().getUserId();
        if (Action.IMAGINE.equals(submitDTO.getAction())) {
            String prompt = submitDTO.getPrompt();
            if (CharSequenceUtil.isBlank(prompt)) {
                return Message.validationError();
            }
            key = task.getId();
            task.setPrompt(prompt);
            String promptEn = this.translateService.translateToEnglish(prompt).trim();
            task.setFinalPrompt("[" + task.getId() + "]" + promptEn);
            task.setDescription("/imagine " + submitDTO.getPrompt());
            this.taskHelper.putState(task.getId(), task.getState());
            this.taskHelper.putTask(userId, task.getId(), task);
            result = this.discordService.imagine(task.getFinalPrompt());
        } else {
            if (CharSequenceUtil.isBlank(submitDTO.getTaskId())) {
                return Message.validationError();
            }
            Task targetTask = this.taskHelper.findById(userId, submitDTO.getTaskId());
            if (targetTask == null) {
                return Message.of(Message.VALIDATION_ERROR_CODE, "任务不存在或已失效");
            }
            if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
                return Message.of(Message.VALIDATION_ERROR_CODE, "关联任务状态错误");
            }
            task.setPrompt(targetTask.getPrompt());
            task.setFinalPrompt(targetTask.getFinalPrompt());
            task.setRelatedTaskId(ConvertUtils.findTaskIdByFinalPrompt(targetTask.getFinalPrompt()));
            key = targetTask.getMessageId() + "-" + submitDTO.getAction();
            this.taskHelper.putState(key, task.getState());
            this.taskHelper.putTask(userId, key, task);
            if (Action.UPSCALE.equals(submitDTO.getAction())) {
                task.setDescription("/up " + submitDTO.getTaskId() + " U" + submitDTO.getIndex());
                result = this.discordService.upscale(targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
            } else if (Action.VARIATION.equals(submitDTO.getAction())) {
                task.setDescription("/up " + submitDTO.getTaskId() + " V" + submitDTO.getIndex());
                result = this.discordService.variation(targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
            } else {
                // todo 暂不支持 reset, 接收mj消息时, 无法找到对应task
                return Message.of(Message.VALIDATION_ERROR_CODE, "暂不支持 reset 操作");
            }
        }
        if (result.getCode() != ResultEnum.SUCCESS.getCode()) {
            this.taskHelper.removeTask(userId, key);
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
        submitDTO.setState(uvSubmitDTO.getState());
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
