package com.github.yi.midjourney.service;


import com.github.yi.midjourney.util.Message;

public interface DiscordService {

    void imagine(String taskId, String prompt);

    void upscale(String taskId, String messageId, int index, String messageHash);

    void variation(String taskId, String messageId, int index, String messageHash);

    void reset(String taskId, String messageId, String messageHash);

    /**
     * 推送生图的数据到mj
     *
     * @param paramsStr 需要生成图的描述词
     * @return 请求的结果
     */
    Message<Void> pushTask(String paramsStr);
}