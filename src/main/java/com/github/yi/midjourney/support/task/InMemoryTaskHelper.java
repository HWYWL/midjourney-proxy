package com.github.yi.midjourney.support.task;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import com.github.yi.midjourney.dto.State;

import java.time.Duration;
import java.util.List;


public class InMemoryTaskHelper implements TaskHelper {

	// 创建缓存
	private final TimedCache<String, Task> taskMap;

	public InMemoryTaskHelper(Duration timeout) {
		taskMap = CacheUtil.newTimedCache(timeout.toMillis());
	}

	@Override
	public void putTask(int userId, String key, Task task) {
		this.taskMap.put(key, task);
	}

	@Override
	public void putState(String key, State state) {

	}

	@Override
	public State getState(String key) {
		return null;
	}

	@Override
	public void removeTask(int userId, String key) {
		this.taskMap.remove(key);
	}

	@Override
	public Task getTask(int userId, String key) {
		return this.taskMap.get(key);
	}

	@Override
	public List<Task> listTask(int userId) {
		return ListUtil.toList(this.taskMap.iterator());
	}
}
