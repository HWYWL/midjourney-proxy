package com.github.yi.midjourney.dto;

import lombok.Data;

/**
 * 结果回调接收参数
 *
 * @author YI
 */
@Data
public class NotifyDTO {
    /**
     * 动作: 必传，IMAGINE（绘图）、UPSCALE（选中放大）、VARIATION（选中变换）
     */
    private String action;

    /**
     * 绘图参数
     * 例如：/imagine 猫猫
     */
    private String description;

    /**
     * 任务id
     */
    private String id;

    /**
     * 绘图参数
     */
    private String prompt;

    /**
     * 透传参数，可以用于区分不同用户
     */
    private State state;

    /**
     * 当前生成的图片的状态
     */
    private String status;

    /**
     * 任务提交时间戳
     */
    private Long submitTime;

    /**
     * 任务结束时间戳
     */
    private Long finishTime;

    /**
     * 生成的图片url
     */
    private String imageUrl;
}
