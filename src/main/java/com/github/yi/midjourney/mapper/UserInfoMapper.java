package com.github.yi.midjourney.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yi.midjourney.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 用户表Mapper
 * @author YI
 * @date 2023-05-10
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}