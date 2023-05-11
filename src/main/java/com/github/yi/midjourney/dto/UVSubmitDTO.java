package com.github.yi.midjourney.dto;

import lombok.Data;

@Data
public class UVSubmitDTO {
	/**
	 * 自定义参数, task中保留.
	 */
	private State state;
	/**
	 * content: id u1.
	 */
	private String content;
	/**
	 * notifyHook of caller
	 */
	private String notifyHook;
}
