package com.github.yi.midjourney.configuration;

import com.github.yi.midjourney.ProxyProperties;
import com.github.yi.midjourney.dto.State;
import com.github.yi.midjourney.support.task.InMemoryTaskHelper;
import com.github.yi.midjourney.support.task.RedisTaskHelper;
import com.github.yi.midjourney.support.task.Task;
import com.github.yi.midjourney.support.task.TaskHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author YI
 */
@Configuration
public class TaskHelperConfig {

    @Bean
    public TaskHelper taskHelper(ProxyProperties proxyProperties, RedisConnectionFactory redisConnectionFactory) {
        ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
        Duration timeout = proxyProperties.getTaskStore().getTimeout();
        RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
        RedisTemplate<String, State> redisStateTemplate = new RedisTemplate<>();

        TaskHelper taskHelper = null;
        switch (type){
            case IN_MEMORY:
                taskHelper = new InMemoryTaskHelper(timeout);
                break;
            case REDIS:
                taskHelper = new RedisTaskHelper(timeout,
                        taskRedisTemplate(redisConnectionFactory, redisTemplate),
                        taskRedisUserTemplate(redisConnectionFactory, redisStateTemplate));
                break;
        }

        return taskHelper;
    }

    public RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory, RedisTemplate<String, Task> redisTemplate) {
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    public RedisTemplate<String, State> taskRedisUserTemplate(RedisConnectionFactory redisConnectionFactory, RedisTemplate<String, State> redisTemplate) {
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

}


