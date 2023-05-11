package com.github.yi.midjourney.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.github.yi.midjourney.util.Message;
import com.github.yi.midjourney.support.task.Task;
import com.github.yi.midjourney.support.task.TaskHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用于获取任务详情接口
 * @author YI
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskHelper taskHelper;

    @SaCheckLogin
    @GetMapping("/list")
    public Message<List<Task>> listTask() {
        int userId = StpUtil.getLoginIdAsInt();
        List<Task> tasks = this.taskHelper.listTask(userId);

        return Message.success(tasks);
    }

    @SaCheckLogin
    @GetMapping("/{id}/fetch")
    public Message<Task> getTask(@PathVariable String id) {
        int userId = StpUtil.getLoginIdAsInt();
        Task task = this.taskHelper.findById(userId, id);
        return Message.success(task);
    }
}
