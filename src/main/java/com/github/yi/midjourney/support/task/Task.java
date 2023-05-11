package com.github.yi.midjourney.support.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.yi.midjourney.dto.State;
import com.github.yi.midjourney.model.Action;
import com.github.yi.midjourney.model.TaskStatus;
import lombok.Data;

/**
 * 任务数据
 * @author YI
 */
@Data
public class Task {

	/**
	 * 需要执行的任务
	 */
	private Action action;
	/**
	 * 任务id
	 */
	private String id;
	/**
	 * 描述词
	 */
	private String prompt;

	/**
	 * 执行描述词语句
	 */
	private String description;
	/**
	 * 自定义参数
	 */
	private State state;
	/**
	 * 任务提交时间戳
	 */
	private Long submitTime;
	/**
	 * 任务完成时间戳
	 */
	private Long finishTime;
	/**
	 * Ai生成的图片地址URL
	 */
	private String imageUrl;
	/**
	 * 任务执行状态
	 */
	private TaskStatus status = TaskStatus.NOT_START;
	/**
	 * 回调接口url
	 */
	private String notifyHook;

	@JsonIgnore
	private String finalPrompt;
	@JsonIgnore
	private String relatedTaskId;
	private String messageId;
	private String messageHash;
}
