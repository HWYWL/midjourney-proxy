package com.github.yi.midjourney.support;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yi.midjourney.ProxyProperties;
import com.github.yi.midjourney.model.Action;
import com.github.yi.midjourney.model.Task;
import com.github.yi.midjourney.model.TaskStatus;
import com.github.yi.midjourney.service.NotifyService;
import com.github.yi.midjourney.service.TaskLogService;
import com.github.yi.midjourney.util.ConvertUtils;
import com.github.yi.midjourney.util.CosUtil;
import com.github.yi.midjourney.util.MessageData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageListener extends ListenerAdapter {
    private final ProxyProperties properties;
    private final NotifyService notifyService;
    private final CosUtil cosUtil;
    private final TaskLogService taskLogService;

    private boolean ignoreMessage(Message message) {
        String authorName = message.getAuthor().getName();
        String channelId = message.getChannel().getId();
        return !this.properties.getDiscord().getMjBotName().equals(authorName) || !this.properties.getDiscord().getChannelId().equals(channelId);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        Message message = event.getMessage();
        if (ignoreMessage(event.getMessage())) {
            return;
        }
        String content = message.getContentRaw();
        log.info("消息变更: {}", content);
        MessageData data = ConvertUtils.matchImagineContent(content);
        if (data == null) {
            data = ConvertUtils.matchUVContent(content);
        }
        if (data == null) {
            return;
        }
        String taskId = ConvertUtils.findTaskIdByFinalPrompt(data.getPrompt());
        String percentage = ConvertUtils.findTaskPercentageByFinalPrompt(content);
        if (CharSequenceUtil.isBlank(taskId)) {
            return;
        }

        // 匹配最后一条指令最新的数据进行更新
        LambdaQueryWrapper<Task> userInfoQueryWrapper = new LambdaQueryWrapper<>();
        userInfoQueryWrapper.eq(Task::getRelatedTaskId, taskId)
                .in(Task::getAction, Arrays.asList(Action.UPSCALE, Action.VARIATION))
                .ne(Task::getTaskStatus, TaskStatus.SUCCESS)
                .orderByDesc(Task::getId);

        Task taskLog = taskLogService.getOne(userInfoQueryWrapper, false);

        if (taskLog == null) {
            userInfoQueryWrapper.clear();
            userInfoQueryWrapper.eq(Task::getTaskId, taskId);
            taskLog = taskLogService.getOne(userInfoQueryWrapper, false);
        }else {
            percentage = "100%";
        }

        // 更新任务进度
        Task task = Task.builder().id(taskLog.getId()).taskProgress(percentage).build();
        taskLogService.updateById(task);
        this.notifyService.notifyTaskChange(taskLog);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (ignoreMessage(event.getMessage())) {
            return;
        }
        String messageId = message.getId();
        String content = message.getContentRaw();
        LambdaQueryWrapper<Task> userInfoQueryWrapper = new LambdaQueryWrapper<>();
        log.info("消息接收: {}", content);
        if (MessageType.SLASH_COMMAND.equals(message.getType()) || MessageType.DEFAULT.equals(message.getType())) {
            MessageData messageData = ConvertUtils.matchImagineContent(content);
            if (messageData == null) {
                return;
            }
            // imagine 命令生成的消息: 启动、完成
            String taskId = ConvertUtils.findTaskIdByFinalPrompt(messageData.getPrompt());
            userInfoQueryWrapper.eq(Task::getTaskId, taskId);
            Task taskLog = taskLogService.getOne(userInfoQueryWrapper);
            if (taskLog == null) {
                return;
            }
            Task task = Task.builder().id(taskLog.getId()).messageId(messageId).build();
            if ("Waiting to start".equals(messageData.getStatus())) {
                task.setTaskStatus(String.valueOf(TaskStatus.IN_PROGRESS));
                taskLogService.updateById(task);
            } else {
                finishTask(task, message);
            }
            this.notifyService.notifyTaskChange(task);
        } else if (MessageType.INLINE_REPLY.equals(message.getType()) && message.getReferencedMessage() != null) {
            MessageData messageData = ConvertUtils.matchUVContent(content);
            if (messageData == null) {
                return;
            }
            // uv 变更图片完成后的消息
            // 匹配最后一条指令最新的数据进行更新
            String taskId = ConvertUtils.findTaskIdByFinalPrompt(content);

            userInfoQueryWrapper.eq(Task::getRelatedTaskId, taskId)
                    .in(Task::getAction, Arrays.asList(Action.UPSCALE, Action.VARIATION))
                    .ne(Task::getTaskStatus, TaskStatus.SUCCESS)
                    .orderByDesc(Task::getId);

            Task taskLog = taskLogService.getOne(userInfoQueryWrapper, false);
            if (taskLog == null) {
                return;
            }

            Task task = Task.builder().id(taskLog.getId())
                    .messageId(messageId).build();
            finishTask(task, message);
            this.notifyService.notifyTaskChange(task);
        }
    }

    private void finishTask(Task task, Message message) {
        DateTime dateTime = DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("Asia/Shanghai"));
        task.setFinishTime(dateTime.toTimestamp());
        if (!message.getAttachments().isEmpty()) {
            task.setTaskStatus(String.valueOf(TaskStatus.SUCCESS));
            task.setTaskProgress("100%");

            String imageUrl = message.getAttachments().get(0).getUrl();
            log.info("图片地址转换开始: {}", imageUrl);
            // 将midjourney图片地址转为腾讯cos文件地址
            String cosUrl = cosUtil.cosUpload(imageUrl);
            log.info("图片地址转换完成: {}", cosUrl);

            task.setImageUrl(cosUrl);
            int hashStartIndex = imageUrl.lastIndexOf("_");
            // .webp or .png
            int hashEndIndex = imageUrl.endsWith(".webp") ? imageUrl.length() - 5 : imageUrl.length() - 4;
            task.setMessageHash(imageUrl.substring(hashStartIndex + 1, hashEndIndex));
        } else {
            task.setTaskStatus(String.valueOf(TaskStatus.FAILURE));
        }

        // 更新数据库
        taskLogService.saveOrUpdate(task);
    }
}
