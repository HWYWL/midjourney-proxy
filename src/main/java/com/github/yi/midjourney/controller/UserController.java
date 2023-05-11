package com.github.yi.midjourney.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.github.yi.midjourney.model.ResultEnum;
import com.github.yi.midjourney.model.UserInfo;
import com.github.yi.midjourney.service.UserInfoService;
import com.github.yi.midjourney.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户登录和校验接口
 *
 * @author YI
 */
@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    UserInfoService userInfoService;

    /**
     * 用户注册
     *
     * @param userInfo 包含用户名和密码的注册信息
     * @return 返回成功/失败信息
     */
    @PostMapping("doRegister")
    public Message<Object> doRegister(@RequestBody UserInfo userInfo) {
        String userName = userInfo.getUserName();
        String password = userInfo.getPassword();
        if (StrUtil.isBlank(userName) || StrUtil.isBlank(password)) {
            return Message.of(ResultEnum.REGISTER_ERROR.getCode(), ResultEnum.REGISTER_ERROR.getMessage());
        }
        boolean existsUser = userInfoService.isExistsUser(userName);
        if (existsUser) {
            return Message.of(ResultEnum.REGISTER_ERROR.getCode(), "用户名已存在");
        }
        userInfoService.userRegister(userName, password);
        return Message.success();
    }

    /**
     * 用户登录
     *
     * @param userInfo 包含用户名和密码的注册信息
     * @return 返回用户id
     */
    @RequestMapping("doLogin")
    public Message<UserInfo> doLogin(@RequestBody UserInfo userInfo) {
        String userName = userInfo.getUserName();
        String password = userInfo.getPassword();
        if (StrUtil.isBlank(userName) || StrUtil.isBlank(password)) {
            return Message.of(ResultEnum.REGISTER_ERROR.getCode(), ResultEnum.REGISTER_ERROR.getMessage());
        }

        UserInfo userLogin = userInfoService.userLogin(userName, password);
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if (userLogin == null) {
            return Message.of(ResultEnum.LOGIN_ERROR.getCode(), ResultEnum.LOGIN_ERROR.getMessage());
        }

        StpUtil.login(userLogin.getId());

        return Message.success(userLogin);
    }

    /**
     * 查询登录状态
     *
     * @return
     */
    @SaCheckLogin
    @RequestMapping("isLogin")
    public Message<Boolean> isLogin() {
        return Message.of(ResultEnum.SUCCESS.getCode(), "已登陆", StpUtil.isLogin());
    }

    /**
     * 当前会话注销登录
     *
     * @return
     */
    @SaCheckLogin
    @RequestMapping("logout")
    public Message<String> logout() {
        StpUtil.logout();
        return Message.of(ResultEnum.SUCCESS.getCode(), "已注销登录");
    }

}
