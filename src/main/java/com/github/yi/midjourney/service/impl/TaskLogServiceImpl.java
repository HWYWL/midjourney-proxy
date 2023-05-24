package com.github.yi.midjourney.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yi.midjourney.mapper.TaskLogMapper;
import com.github.yi.midjourney.model.Task;
import com.github.yi.midjourney.service.TaskLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TimeZone;

/**
 * @author YI
 */
@Service
public class TaskLogServiceImpl extends ServiceImpl<TaskLogMapper, Task> implements TaskLogService {

    @Override
    public boolean saveOrUpdateTask(Task task) {
        UpdateWrapper<Task> updateWrapper = Wrappers.update();
        Integer id = task.getId();
        if (id != null) {
            return this.saveOrUpdate(task);
        } else {
            updateWrapper.lambda().eq(Task::getTaskId, task.getTaskId()).eq(Task::getUserId, task.getUserId());
            return this.saveOrUpdate(task, updateWrapper);
        }
    }

    @Override
    public Task getOneByTaskId(String taskId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getTaskId, taskId);
        return this.getOne(wrapper);
    }

    @Override
    public List<Task> getListTaskByUserId(int userId) {
        DateTime offset = DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("Asia/Shanghai")).offset(DateField.DAY_OF_YEAR, -30);
        //分页参数
        Page<Task> page = Page.of(1,30);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId);
        wrapper.eq(Task::getTaskStatus, "SUCCESS");
        wrapper.gt(Task::getSubmitTime, offset.toTimestamp());
        wrapper.orderByDesc(Task::getSubmitTime);

        Page<Task> taskPage = this.baseMapper.selectPage(page, wrapper);

        return taskPage.getRecords();
    }
}
