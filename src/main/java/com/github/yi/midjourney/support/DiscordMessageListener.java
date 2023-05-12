package com.github.yi.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.yi.midjourney.ProxyProperties;
import com.github.yi.midjourney.dto.State;
import com.github.yi.midjourney.model.Action;
import com.github.yi.midjourney.model.TaskStatus;
import com.github.yi.midjourney.service.NotifyService;
import com.github.yi.midjourney.support.task.Task;
import com.github.yi.midjourney.support.task.TaskHelper;
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
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageListener extends ListenerAdapter {
    private final ProxyProperties properties;
    private final TaskHelper taskHelper;
    private final NotifyService notifyService;

    private final CosUtil cosUtil;

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
        log.debug("消息变更: {}", content);
        MessageData data = ConvertUtils.matchImagineContent(content);
        if (data == null) {
            data = ConvertUtils.matchUVContent(content);
        }
        if (data == null) {
            return;
        }
        String relatedTaskId = ConvertUtils.findTaskIdByFinalPrompt(data.getPrompt());
        if (CharSequenceUtil.isBlank(relatedTaskId)) {
            return;
        }
        List<Action> uvActions = Arrays.asList(Action.UPSCALE, Action.VARIATION);
        State state = this.taskHelper.getState(relatedTaskId);
        Task task = this.taskHelper.listTask(state.getUserId()).stream()
                .filter(t -> relatedTaskId.equals(t.getRelatedTaskId())
                        && TaskStatus.NOT_START.equals(t.getStatus())
                        && uvActions.contains(t.getAction()))
                .max(Comparator.comparing(Task::getSubmitTime))
                .orElse(null);
        if (task == null) {
            return;
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        this.notifyService.notifyTaskChange(task);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (ignoreMessage(event.getMessage())) {
            return;
        }
        String messageId = message.getId();
        String content = message.getContentRaw();
        log.debug("消息接收: {}", content);
        if (MessageType.SLASH_COMMAND.equals(message.getType()) || MessageType.DEFAULT.equals(message.getType())) {
            MessageData messageData = ConvertUtils.matchImagineContent(content);
            if (messageData == null) {
                return;
            }
            // imagine 命令生成的消息: 启动、完成
            String taskId = ConvertUtils.findTaskIdByFinalPrompt(messageData.getPrompt());
            State state = this.taskHelper.getState(taskId);
            Task task = this.taskHelper.getTask(state.getUserId(), taskId);
            if (task == null) {
                return;
            }
            task.setMessageId(messageId);
            if ("Waiting to start".equals(messageData.getStatus())) {
                task.setStatus(TaskStatus.IN_PROGRESS);
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
            String taskId = message.getReferencedMessage().getId();
            State state = this.taskHelper.getState(taskId + "-" + messageData.getAction());
            Task task = this.taskHelper.getTask(state.getUserId(), taskId + "-" + messageData.getAction());
            if (task == null) {
                return;
            }
            task.setMessageId(messageId);
            finishTask(task, message);
            this.notifyService.notifyTaskChange(task);
        }
    }

    private void finishTask(Task task, Message message) {
        task.setFinishTime(System.currentTimeMillis());
        if (!message.getAttachments().isEmpty()) {
            task.setStatus(TaskStatus.SUCCESS);
            String imageUrl = message.getAttachments().get(0).getUrl();
            log.debug("图片地址转换开始: {}", imageUrl);
            // 将midjourney图片地址转为腾讯cos文件地址
            String cosUrl = cosUtil.cosUpload(imageUrl);
            log.debug("图片地址转换完成: {}", cosUrl);
            task.setImageUrl(cosUrl);
            int hashStartIndex = imageUrl.lastIndexOf("_");
            // .webp or .png
            int hashEndIndex = imageUrl.endsWith(".webp") ? imageUrl.length() - 5 : imageUrl.length() - 4;
            task.setMessageHash(imageUrl.substring(hashStartIndex + 1, hashEndIndex));
        } else {
            task.setStatus(TaskStatus.FAILURE);
        }
        State state = task.getState();
        taskHelper.putTask(state.getUserId(), task.getId(), task);
    }

}
