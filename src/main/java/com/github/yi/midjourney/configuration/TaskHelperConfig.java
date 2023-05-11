package com.github.yi.midjourney.configuration;

import com.github.yi.midjourney.ProxyProperties;
import com.github.yi.midjourney.dto.State;
import com.github.yi.midjourney.service.TranslateService;
import com.github.yi.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.yi.midjourney.service.translate.GPTTranslateServiceImpl;
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
        return switch (type) {
            case IN_MEMORY -> new InMemoryTaskHelper(timeout);
            case REDIS -> new RedisTaskHelper(timeout,
                    taskRedisTemplate(redisConnectionFactory, redisTemplate),
                    taskRedisUserTemplate(redisConnectionFactory, redisStateTemplate));
        };
    }

    @Bean
    TranslateService translateService(ProxyProperties properties) {
        return switch (properties.getTranslateWay()) {
            case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
            case GPT -> new GPTTranslateServiceImpl(properties.getOpenai());
            default -> prompt -> prompt;
        };
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


