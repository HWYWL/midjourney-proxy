package com.github.yi.midjourney.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: YI
 * @description:  Mybatis Plus配置
 * @date: create in 2023年5月10日15:53:37
 */
@Configuration
@MapperScan(basePackages = {"com.github.yi.midjourney.mapper"})
public class MybatisPlusConf {
}
