package com.github.yi.midjourney.dto;

import com.github.yi.midjourney.model.Action;
import lombok.Data;

@Data
public class SubmitDTO {
    /**
     * 动作: IMAGINE\UPSCALE\VARIATION\RESET.
     */
    private Action action;
    /**
     * prompt: action 为 IMAGINE 必传.
     */
    private String prompt;
    /**
     * Midjourney额外参数,例如版本选择4或者5，图片比例1:1、4:3
     */
    private String extraParam;
    /**
     * 任务ID: action 为 UPSCALE\VARIATION\RESET 必传.
     */
    private String taskId;
    /**
     * index: action 为 UPSCALE\VARIATION 必传.
     */
    private Integer index;
    /**
     * 任务处理完毕的回调函数
     */
    private String notifyHook;
}
