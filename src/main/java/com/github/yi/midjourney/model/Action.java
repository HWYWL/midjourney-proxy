package com.github.yi.midjourney.model;


/**
 * 需要执行的任务
 * @author YI
 */

public enum Action {
	/**
	 * 生成图片.
	 */
	IMAGINE,
	/**
	 * 选中放大.
	 */
	UPSCALE,
	/**
	 * 选中其中的一张图，生成四张相似的.
	 */
	VARIATION,
	/**
	 * 重新生成.
	 */
	RESET

}
