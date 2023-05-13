package com.github.yi.midjourney.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yi.midjourney.model.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 任务表Mapper
 * @author YI
 * @date 2023-05-13
 */
@Mapper
public interface TaskLogMapper extends BaseMapper<Task> {
}
