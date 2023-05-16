package com.github.yi.midjourney.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.github.yi.midjourney.service.TaskLogService;
import com.github.yi.midjourney.util.Message;
import com.github.yi.midjourney.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用于获取任务详情接口
 *
 * @author YI
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@CrossOrigin
public class TaskController {
    private final TaskLogService taskLogService;

    @SaCheckLogin
    @GetMapping("/list")
    public Message<List<Task>> listTask() {
        int userId = StpUtil.getLoginIdAsInt();
        List<Task> tasks = this.taskLogService.getListTaskByUserId(userId);

        return Message.success(tasks);
    }

    @SaCheckLogin
    @GetMapping("/{id}/fetch")
    public Message<Task> getTask(@PathVariable String id) {
        Task task = this.taskLogService.getOneByTaskId(id);

        return Message.success(task);
    }
}
