package com.github.yi.midjourney.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yi.midjourney.configuration.Constant;
import com.github.yi.midjourney.mapper.UserInfoMapper;
import com.github.yi.midjourney.model.UserInfo;
import com.github.yi.midjourney.service.UserInfoService;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

/**
 * 操作用户数据逻辑
 *
 * @author YI
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Override
    public void userRegister(String username, String password) {
        DateTime dateTime = DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("Asia/Shanghai"));

        // 加密用户密码
        HMac mac = SecureUtil.hmacMd5(Constant.USER_PASSWORD_SECRET);
        String macHexPassword = mac.digestHex(password);

        UserInfo userInfo = UserInfo.builder()
                .userName(username).password(macHexPassword)
                .vipStartTime(dateTime.toTimestamp()).vipEndTime(dateTime.offsetNew(DateField.DAY_OF_YEAR, Constant.USER_REGISTER_VIP_DAY).toTimestamp())
                .build();
        this.save(userInfo);
    }

    @Override
    public UserInfo userLogin(String username, String password) {
        LambdaQueryWrapper<UserInfo> userInfoQueryWrapper = new LambdaQueryWrapper<>();
        userInfoQueryWrapper.eq(UserInfo::getUserName, username);

        UserInfo userInfo = this.getOne(userInfoQueryWrapper);
        if (userInfo != null) {
            String pw = userInfo.getPassword();
            // 加密用户密码
            HMac mac = SecureUtil.hmacMd5(Constant.USER_PASSWORD_SECRET);
            String macHexPassword = mac.digestHex(password);

            userInfo.setPassword(null);
            //密码正确返回用户数据，密码错误返回null
            return pw.equals(macHexPassword) ? userInfo : null;
        }

        return null;
    }

    @Override
    public boolean isExistsUser(String username) {
        LambdaQueryWrapper<UserInfo> userInfoQueryWrapper = new LambdaQueryWrapper<>();
        userInfoQueryWrapper.eq(UserInfo::getUserName, username);
        long count = this.count(userInfoQueryWrapper);

        return count > 0;
    }
}
