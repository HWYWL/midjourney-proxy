package com.github.yi.midjourney.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yi.midjourney.configuration.Constant;
import com.github.yi.midjourney.model.UserInfo;
import com.github.yi.midjourney.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * 自定义权限验证接口扩展
 *
 * @author YI
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    UserInfoService userInfoService;

    /**
     * 获取账号权限
     *
     * @param loginId   账号id，即你在调用 StpUtil.login(id) 时写入的标识值。
     * @param loginType 账号体系标识，此处可以暂时忽略，在 [ 多账户认证 ] 章节下会对这个概念做详细的解释。
     * @return 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return getAuthorityLists(loginId);
    }

    /**
     * 获取角色权限
     *
     * @param loginId   账号id，即你在调用 StpUtil.login(id) 时写入的标识值。
     * @param loginType 账号体系标识，此处可以暂时忽略，在 [ 多账户认证 ] 章节下会对这个概念做详细的解释。
     * @return 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return getAuthorityLists(loginId);
    }

    /**
     * 获取账号拥有的权限
     *
     * @param userId 用户id
     * @return 返回权限列表
     */
    private List<String> getAuthorityLists(Object userId) {
        LambdaQueryWrapper<UserInfo> userInfoQueryWrapper = new LambdaQueryWrapper<>();
        userInfoQueryWrapper.eq(UserInfo::getId, userId);

        UserInfo userInfo = userInfoService.getOne(userInfoQueryWrapper);
        long vipEndTime = userInfo.getVipEndTime().getTime();
        long nowTime = DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("Asia/Shanghai")).getTime();

        // 如果VIP到期时间大于现在，则赋予访问权限
        List<String> authoritys = new ArrayList<>();
        if (nowTime < vipEndTime) {
            authoritys.add(Constant.USER_ACCESS_AUTHORITY_VIP);
        }

        return authoritys;
    }
}
