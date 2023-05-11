package com.github.yi.midjourney.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yi.midjourney.model.UserInfo;


/**
 * @author YI
 * @description 用户表服务层
 * @date 2023-05-10
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     */
    void userRegister(String username, String password);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 返回用户信息
     */
    UserInfo userLogin(String username, String password);

    /**
     * 判断用户名是否已存在
     *
     * @param username 用户名
     * @return 存在：true、不存在：false
     */
    boolean isExistsUser(String username);
}
