package com.github.yi.midjourney.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 任务数据
 *
 * @author YI
 */
@TableName("task_log")
@Builder
@Data
@AllArgsConstructor
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 需要执行的任务
     */
    private String action;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 描述词
     */
    private String prompt;

    /**
     * 执行的描述词
     */
    private String description;

    /**
     * 执行任务的用户id
     */
    private Integer userId;

    /**
     * 任务提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp submitTime;

    /**
     * 任务完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp finishTime;

    /**
     * ai生成的图片url
     */
    private String imageUrl;

    /**
     * 任务执行状态
     */
    private String taskStatus;

    /**
     * 任务进度
     */
    private String taskProgress;

    /**
     * 回调接口
     */
    private String notifyHook;

    /**
     * 聊天的id
     */
    private String messageId;

    /**
     * 消息的hash
     */
    private String messageHash;

    /**
     * 最终地执行关键词
     */
    private String finalPrompt;

    /**
     * 关联的任务id
     */
    private String relatedTaskId;
}
