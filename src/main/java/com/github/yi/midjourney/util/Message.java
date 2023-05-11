package com.github.yi.midjourney.util;

import com.github.yi.midjourney.model.ResultEnum;
import lombok.Getter;

/**
 * 返回值封装
 * @author YI
 */
@Getter
public class Message<T> {
    private final int code;
    private final String description;
    private final T result;

    public static final int NOT_FOUND_CODE = 3;
    public static final int VALIDATION_ERROR_CODE = 4;
    public static final int FAILURE_CODE = 9;

    public static <Y> Message<Y> success() {
        return new Message<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage());
    }

    public static <T> Message<T> success(T result) {
        return new Message<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), result);
    }

    public static <Y> Message<Y> notFound() {
        return new Message<>(ResultEnum.UNKNOWN_ERROR.getCode(), ResultEnum.UNKNOWN_ERROR.getMessage());
    }

    public static <Y> Message<Y> validationError() {
        return new Message<>(VALIDATION_ERROR_CODE, "校验错误");
    }

    public static <Y> Message<Y> failure() {
        return new Message<>(FAILURE_CODE, "系统异常");
    }

    public static <Y> Message<Y> of(int code, String description) {
        return new Message<>(code, description);
    }

    public static <T> Message<T> of(int code, String description, T result) {
        return new Message<>(code, description, result);
    }

    private Message(int code, String description) {
        this(code, description, null);
    }

    private Message(int code, String description, T result) {
        this.code = code;
        this.description = description;
        this.result = result;
    }
}
