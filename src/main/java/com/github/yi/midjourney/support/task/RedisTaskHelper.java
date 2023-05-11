package com.github.yi.midjourney.support.task;

import com.github.yi.midjourney.dto.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用Redis存储任务数据
 *
 * @author YI
 */
@RequiredArgsConstructor
public class RedisTaskHelper implements TaskHelper {
    private final Duration timeout;

    private final RedisTemplate<String, Task> redisTemplate;
    private final RedisTemplate<String, State> redisUserTemplate;

    @Override
    public void putTask(int userId, String key, Task task) {
        this.redisTemplate.opsForValue().set(getRedisKey(userId, key), task, timeout);
    }

    @Override
    public void putState(String key, State state) {
        this.redisUserTemplate.opsForValue().set(getRedisUserKey(key), state, timeout);
    }

    @Override
    public State getState(String key) {
        return this.redisUserTemplate.opsForValue().get(getRedisUserKey(key));
    }

    @Override
    public void removeTask(int userId, String key) {
        this.redisTemplate.delete(getRedisKey(userId, key));
    }

    @Override
    public Task getTask(int userId, String key) {
        return this.redisTemplate.opsForValue().get(getRedisKey(userId, key));
    }

    @Override
    public List<Task> listTask(int userId) {
        Set<String> keys = this.redisTemplate.keys(getRedisKey(userId, "*"));

        Set<String> collect = keys.stream().filter(e -> !e.contains("-")).collect(Collectors.toSet());
        return processTask(collect);
    }

    private List<Task> processTask(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        ValueOperations<String, Task> operations = this.redisTemplate.opsForValue();
        return keys.stream().map(operations::get)
                .filter(Objects::nonNull)
                .toList();
    }


    private String getRedisKey(int userId, String key) {
        return "mj::task::" + userId + "::" + key;
    }

    private String getRedisUserKey(String key) {
        return "user::task::" + key;
    }
}
