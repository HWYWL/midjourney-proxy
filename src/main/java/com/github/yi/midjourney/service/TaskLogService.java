package com.github.yi.midjourney.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yi.midjourney.model.Task;

import java.util.List;

/**
 * @author YI
 * @description 任务表服务层
 * @date 2023-05-13
 */
public interface TaskLogService extends IService<Task> {
    /**
     * 更新或保存任务日志
     *
     * @param task 任务日志
     * @return
     */
    boolean saveOrUpdateTask(Task task);

    /**
     * 根据任务id获取任务日志
     *
     * @param taskId 任务id
     * @return 任务日志
     */
    Task getOneByTaskId(String taskId);

    /**
     * 根据userId获取该用户最近30天内的30条任务日志
     *
     * @param userId 用户id
     * @return 任务日志
     */
    List<Task> getListTaskByUserId(int userId);
}
