package com.github.yi.midjourney.aspect;

import cn.hutool.json.JSONUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * @author: YI
 * @description: 接口请求拦截
 * @date: create in 2023年5月10日10:20:15
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 两个..代表所有子目录，最后括号里的两个..代表所有参数
     */
    @Pointcut("execution( * com.github.yi.midjourney.controller..*(..))")
    public void logPointCut() {

    }

    @Before("logPointCut()")
    public void doBefore(JoinPoint joinPoint) {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Map<String, String[]> parameterMap = request.getParameterMap();
        // 记录下请求内容
        logger.info("请求地址 : " + request.getRequestURL().toString());
        logger.info("HTTP METHOD : " + request.getMethod());
        logger.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "."
                + joinPoint.getSignature().getName());
        logger.info("参数 : " + Arrays.toString(joinPoint.getArgs()));
        logger.info("参数 : " + joinPoint.getArgs());
        logger.info("请求参数 : " + JSONUtil.toJsonStr(parameterMap));
    }

    /**
     * returning的值和doAfterReturning的参数名一致
     *
     * @param ret
     */
    @AfterReturning(returning = "ret", pointcut = "logPointCut()")
    public void doAfterReturning(Object ret) {
        // 处理完请求，返回内容(返回值太复杂时，打印的是物理存储空间的地址)
        logger.info("返回值 : " + ret);
    }

    /**
     * 环绕通知,环绕增强
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("logPointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        // ob 为方法的返回值
        Object ob = pjp.proceed();
        logger.info("耗时 : " + (System.currentTimeMillis() - startTime));

        return ob;
    }
}
