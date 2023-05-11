package com.github.yi.midjourney.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.core.util.StrUtil;
import com.github.yi.midjourney.model.ResultEnum;
import com.github.yi.midjourney.util.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * @author: YI
 * @description: 校验统一异常处理
 * @date: create in 2021/2/27 12:02
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 忽略参数异常处理器
     *
     * @param e 忽略参数异常
     * @return MessageResult
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Message<String> parameterMissingExceptionHandler(MissingServletRequestParameterException e) {
        log.error("忽略参数异常", e);

        return Message.of(ResultEnum.PARAMETER_ERROR.getCode(), ResultEnum.PARAMETER_ERROR.getMessage(), e.getParameterName());
    }

    /**
     * 缺少请求体异常处理器
     *
     * @param e 缺少请求体异常
     * @return MessageResult
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Message<String> parameterBodyMissingExceptionHandler(HttpMessageNotReadableException e) {
        log.error("缺少请求体异常", e);

        return Message.of(ResultEnum.PARAMETER_ERROR.getCode(), ResultEnum.PARAMETER_ERROR.getMessage());
    }

    /**
     * 参数效验异常处理器
     *
     * @param e 参数验证异常
     * @return MessageResult
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Message<String> parameterExceptionHandler(MethodArgumentNotValidException e) {
        log.error("参数效验异常", e);
        // 获取异常信息
        BindingResult exceptions = e.getBindingResult();
        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                // 这里列出了全部错误参数，按正常逻辑，只需要第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return Message.of(ResultEnum.PARAMETER_ERROR.getCode(), fieldError.getDefaultMessage(), fieldError.getField());
            }
        }
        return Message.of(ResultEnum.PARAMETER_ERROR.getCode(), ResultEnum.PARAMETER_ERROR.getMessage());
    }

    /**
     * 未登录异常处理器
     *
     * @param e 自定义参数
     * @return MessageResult
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({NotLoginException.class})
    public Message<String> notLoginExceptionHandler(NotLoginException e) {
        log.warn("账号未登录", e);
        return Message.of(ResultEnum.LOGIN_EXPIRE.getCode(), ResultEnum.LOGIN_EXPIRE.getMessage());
    }

    /**
     * 没有权限异常处理器
     *
     * @param e 自定义参数
     * @return MessageResult
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({NotRoleException.class})
    public Message<String> notRoleExceptionHandler(NotRoleException e) {
        log.warn("账号非vip", e);
        return Message.of(ResultEnum.LOGIN_NOT_VIP.getCode(), ResultEnum.LOGIN_NOT_VIP.getMessage());
    }

    /**
     * 其他异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public Message<String> otherExceptionHandler(Exception e) {
        log.error(ResultEnum.UNKNOWN_ERROR.getMessage(), e);

        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
        Throwable cause = e.getCause();
        if (cause != null && StrUtil.isNotBlank(e.getCause().getMessage())) {
            String message = e.getCause().getMessage();
            return Message.of(ResultEnum.UNKNOWN_ERROR.getCode(), message);
        } else if (StrUtil.isNotBlank(e.getMessage())) {
            return Message.of(ResultEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
        return Message.of(ResultEnum.UNKNOWN_ERROR.getCode(), ResultEnum.UNKNOWN_ERROR.getMessage());
    }
}
