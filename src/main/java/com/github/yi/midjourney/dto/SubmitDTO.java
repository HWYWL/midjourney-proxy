package com.github.yi.midjourney.dto;

import com.github.yi.midjourney.model.Action;
import lombok.Data;

@Data
public class SubmitDTO {
	/**
	 * state: 自定义参数, task中保留.
	 */
	private State state;
	/**
	 * 动作: IMAGINE\UPSCALE\VARIATION\RESET.
	 */
	private Action action;
	/**
	 * prompt: action 为 IMAGINE 必传.
	 */
	private String prompt;
	/**
	 * 任务ID: action 为 UPSCALE\VARIATION\RESET 必传.
	 */
	private String taskId;
	/**
	 * index: action 为 UPSCALE\VARIATION 必传.
	 */
	private Integer index;
	/**
	 * notifyHook of caller
	 */
	private String notifyHook;
}
