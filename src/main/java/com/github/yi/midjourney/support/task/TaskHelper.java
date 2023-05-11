package com.github.yi.midjourney.support.task;


import com.github.yi.midjourney.dto.State;

import java.util.List;

public interface TaskHelper {

    void putTask(int userId, String key, Task task);

    void putState(String key, State state);

    State getState(String key);

    void removeTask(int userId, String key);

    Task getTask(int userId, String key);

    List<Task> listTask(int userId);

    default Task findById(int userId, String taskId) {
        return listTask(userId).stream()
                .filter(task -> taskId.equals(task.getId()))
                .findFirst().orElse(null);
    }
}
