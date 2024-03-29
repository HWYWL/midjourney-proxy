package com.github.yi.midjourney.service.impl;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.github.yi.midjourney.ProxyProperties;
import com.github.yi.midjourney.configuration.Constant;
import com.github.yi.midjourney.service.DiscordService;
import com.github.yi.midjourney.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordServiceImpl implements DiscordService {
    private final ProxyProperties properties;

    private static final String DISCORD_API_URL = "https://discord.com/api/v9/interactions";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";

    private String imagineParamsJson;
    private String upscaleParamsJson;
    private String variationParamsJson;
    private String resetParamsJson;

    private String discordUserToken;
    private String discordGuildId;
    private String discordChannelId;

    @PostConstruct
    void init() {
        this.discordUserToken = this.properties.getDiscord().getUserToken();
        this.discordGuildId = this.properties.getDiscord().getGuildId();
        this.discordChannelId = this.properties.getDiscord().getChannelId();
        try {
            this.imagineParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/imagine.json").openStream());
            this.upscaleParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/upscale.json").openStream());
            this.variationParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/variation.json").openStream());
            this.resetParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/reset.json").openStream());
        } catch (IOException e) {
            // can't happen
        }
    }

    @Override
    public void imagine(String taskId, String prompt) {
        String paramsStr = this.imagineParamsJson.replace("$guild_id", this.discordGuildId)
                .replace("$channel_id", this.discordChannelId);
        JSONObject params = new JSONObject(paramsStr);
        params.getJSONObject("data").getJSONArray("options").getJSONObject(0)
                .set("value", prompt);

        generateTaskQueue(taskId, params.toString());
    }

    @Override
    public void upscale(String taskId, String messageId, int index, String messageHash) {
        String paramsStr = this.upscaleParamsJson.replace("$guild_id", this.discordGuildId)
                .replace("$channel_id", this.discordChannelId)
                .replace("$message_id", messageId)
                .replace("$index", String.valueOf(index))
                .replace("$message_hash", messageHash);

        generateTaskQueue(taskId, paramsStr);
    }

    @Override
    public void variation(String taskId, String messageId, int index, String messageHash) {
        String paramsStr = this.variationParamsJson.replace("$guild_id", this.discordGuildId)
                .replace("$channel_id", this.discordChannelId)
                .replace("$message_id", messageId)
                .replace("$index", String.valueOf(index))
                .replace("$message_hash", messageHash);

        generateTaskQueue(taskId, paramsStr);
    }

    @Override
    public void reset(String taskId, String messageId, String messageHash) {
        String paramsStr = this.resetParamsJson.replace("$guild_id", this.discordGuildId)
                .replace("$channel_id", this.discordChannelId)
                .replace("$message_id", messageId)
                .replace("$message_hash", messageHash);

        generateTaskQueue(taskId, paramsStr);
    }

    @Override
    public Message<Void> pushTask(String paramsStr) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", this.discordUserToken);
        headers.add("user-agent", USER_AGENT);
        HttpEntity<String> httpEntity = new HttpEntity<>(paramsStr, headers);
        try {
            ResponseEntity<String> responseEntity = new RestTemplate().postForEntity(DISCORD_API_URL, httpEntity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                return Message.success();
            }
            return Message.of(responseEntity.getStatusCodeValue(), CharSequenceUtil.sub(responseEntity.getBody(), 0, 100));
        } catch (HttpClientErrorException e) {
            try {
                JSONObject error = new JSONObject(e.getResponseBodyAsString());
                return Message.of(error.getInt("code", e.getRawStatusCode()), error.getStr("message"));
            } catch (Exception je) {
                return Message.of(e.getRawStatusCode(), CharSequenceUtil.sub(e.getMessage(), 0, 100));
            }
        }
    }

    /**
     * 添加任务队列数据
     *
     * @param taskId    任务id
     * @param paramsStr 需要生成图的描述词
     */
    private void generateTaskQueue(String taskId, String paramsStr) {
        Map<String, String> myMap = new HashMap<String, String>(1) {{
            put(taskId, paramsStr);
        }};

        if (Constant.ATOMIC_INT.get() >= Constant.GENERATE_TASK_CONCURRENCY_QUEUE_NUM) {
            Constant.taskQueue.add(myMap);
        } else {
            pushTask(paramsStr);
            Constant.ATOMIC_INT.incrementAndGet();
        }
    }
}
