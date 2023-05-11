package com.github.yi.midjourney.configuration;

/**
 * 全局常量
 *
 * @author yi
 */
public class Constant {
    /**
     * 用户密码加密密钥
     */
    public static final String USER_PASSWORD_SECRET = "@4#$1^6*()";

    /**
     * 新注册用户的vip天数，默认为1天
     */
    public static final int USER_REGISTER_VIP_DAY = 1;

    /**
     * 用户访问权限
     */
    public static final String USER_ACCESS_AUTHORITY_VIP = "VIP";

    /**
     * 腾讯云存储对象地址前缀
     */
    public static final String COS_STORAGE_ADDRESS_PREFIX = "https://lg-6pthrhxo-1253466000.cos.ap-shanghai.myqcloud.com/";
}
