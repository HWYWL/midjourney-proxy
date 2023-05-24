package com.github.yi.midjourney.configuration;

import cn.hutool.core.img.ImgUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局常量
 *
 * @author yi
 */
public class Constant {
    /**
     * 用于执行mj绘画生成的任务队列，控制超过mj的并发防止卡死
     */
    public static Queue<Map<String, String>> taskQueue = new LinkedList<>();

    /**
     * 生成MJ任务并发数
     */
    public static final int GENERATE_TASK_CONCURRENCY_QUEUE_NUM = 3;

    /**
     * 生成MJ任务并发计数
     */
    public static final AtomicInteger ATOMIC_INT = new AtomicInteger(0);

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

    /**
     * 支持的文件上传格式
     */
    public static final List<String> IMAGE_TYPES = Arrays.asList(ImgUtil.IMAGE_TYPE_JPEG, ImgUtil.IMAGE_TYPE_JPG, ImgUtil.IMAGE_TYPE_PNG);
}
