package com.github.yi.midjourney.model;

/**
 * @author: YI
 * @description: 返回信息的枚举类
 * @date: create in 2023年5月10日10:22:38
 */
public enum ResultEnum {
    SUCCESS(200, "请求成功"),
    PARAMETER_ERROR(1001, "请求参数有误!"),

    SIGN_ERROR(1003, "签名验证错误!"),
    INVALID_SING(1004, "sing签名为空!"),
    INVALID_TIMESTAMP(1005, "timestamp为空或为非时间戳!"),
    SIGN_TIME_OUT_ERROR(1006, "签名时间已超过24小时，请重新生成!"),
    JSON_ANALYSIS_ERROR(1007, "JSON解析错误!"),
    REGISTER_ERROR(1008, "注册失败，请检查用户名和密码是否为空!"),
    LOGIN_ERROR(1009, "登陆失败，请检查用户名和密码!"),
    LOGIN_EXPIRE(1010, "登陆已过期，请重新登录!"),
    LOGIN_NOT_VIP(1011, "VIP已过期，暂无法使用此功能!"),
    UNKNOWN_ERROR(9999, "未知的错误!");

    private Integer code;
    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ResultEnum fromCode(Integer code) {
        for (ResultEnum telecomType : values()) {
            if (telecomType.code.equals(code)) {
                return telecomType;
            }
        }
        return null;
    }

    public static ResultEnum fromMessage(String message) {
        for (ResultEnum telecomType : values()) {
            if (telecomType.message.equals(message)) {
                return telecomType;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
