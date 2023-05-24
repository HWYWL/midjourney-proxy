package com.github.yi.midjourney.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.github.yi.midjourney.configuration.Constant;
import com.github.yi.midjourney.configuration.CosConfig;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Component
public class CosUtil {
    @Autowired
    TransferManager transferManager;
    @Autowired
    CosConfig cosConfig;

    /**
     * 将midjourney生成的图片上传到cos
     *
     * @param imageUrl midjourney图片地址
     * @return 腾讯cos图片地址
     */
    public String cosUpload(String imageUrl) {
        try {
            // 对象键(Key)是对象在存储桶中的唯一标识。
            String[] split = imageUrl.split(StrUtil.UNDERLINE);

            File tempFile = FileUtil.createTempFile();
            File file = HttpUtil.downloadFileFromUrl(imageUrl, tempFile, 10000);

            String today = DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("Asia/Shanghai")).toString(DatePattern.NORM_DATE_PATTERN);
            String key = "midjourney/" + today + StrUtil.SLASH + split[split.length - 1];
            imageUrl = uploadFile(file, key);
        } catch (Exception e) {
            log.error("文件转换异常，将使用原有url，错误：{}", e.getMessage());
        }

        // 拼接url返回
        return imageUrl;
    }

    /**
     * 将本地文件上传到腾讯云
     *
     * @param file      本地图片文件
     * @param imageType 图片格式
     * @return 腾讯cos图片地址
     */
    public String cosUpload(MultipartFile file, String imageType) throws IOException {
        String today = DateUtil.convertTimeZone(DateUtil.date(), TimeZone.getTimeZone("Asia/Shanghai")).toString(DatePattern.NORM_DATE_PATTERN);
        String key = "userImages/" + today + StrUtil.SLASH + RandomUtil.randomString(16) + StrUtil.DOT + imageType;
        // 拼接url返回
        return uploadFile(file, key);
    }

    private String uploadFile(MultipartFile file, String key) throws IOException {
        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosConfig.getBucketName();

        //若需要设置对象的自定义 Headers 可参照下列代码,若不需要可省略下面这几行,对象自定义 Headers 的详细信息可参考 https://cloud.tencent.com/document/product/436/13361
        ObjectMetadata objectMetadata = new ObjectMetadata();

        //若设置 Content-Type、Cache-Control、Content-Disposition、Content-Encoding、Expires 这五个字自定义 Headers，推荐采用 objectMetadata.setHeader()
        objectMetadata.setHeader(key, "Content-Type");
        //若要设置 “x-cos-meta-[自定义后缀]” 这样的自定义 Header，推荐采用
        Map<String, String> userMeta = new HashMap<>(16);
        userMeta.put("x-cos-meta-mj", String.valueOf(key.hashCode()));
        objectMetadata.setUserMetadata(userMeta);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), objectMetadata);

        // 设置存储类型（如有需要，不需要请忽略此行代码）, 默认是标准(Standard), 低频(standard_ia)
        // 更多存储类型请参见 https://cloud.tencent.com/document/product/436/33417
        putObjectRequest.setStorageClass(StorageClass.Standard_IA);
        putObjectRequest.withMetadata(objectMetadata);

        String cosKey = null;
        try {
            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回 UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);

            UploadResult uploadResult = upload.waitForUploadResult();
            cosKey = uploadResult.getKey();
        } catch (CosClientException | InterruptedException e) {
            e.printStackTrace();
        }

        // 拼接url返回
        return StrUtil.isBlank(cosKey) ? cosKey : Constant.COS_STORAGE_ADDRESS_PREFIX + cosKey;
    }

    private String uploadFile(File file, String key) {
        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosConfig.getBucketName();

        //若需要设置对象的自定义 Headers 可参照下列代码,若不需要可省略下面这几行,对象自定义 Headers 的详细信息可参考 https://cloud.tencent.com/document/product/436/13361
        ObjectMetadata objectMetadata = new ObjectMetadata();

        //若设置 Content-Type、Cache-Control、Content-Disposition、Content-Encoding、Expires 这五个字自定义 Headers，推荐采用 objectMetadata.setHeader()
        objectMetadata.setHeader(key, "Content-Type");
        //若要设置 “x-cos-meta-[自定义后缀]” 这样的自定义 Header，推荐采用
        Map<String, String> userMeta = new HashMap<>(16);
        userMeta.put("x-cos-meta-mj", String.valueOf(key.hashCode()));
        objectMetadata.setUserMetadata(userMeta);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);

        // 设置存储类型（如有需要，不需要请忽略此行代码）, 默认是标准(Standard), 低频(standard_ia)
        // 更多存储类型请参见 https://cloud.tencent.com/document/product/436/33417
        putObjectRequest.setStorageClass(StorageClass.Standard_IA);
        putObjectRequest.withMetadata(objectMetadata);

        String cosKey = null;
        try {
            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回 UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);

            UploadResult uploadResult = upload.waitForUploadResult();
            cosKey = uploadResult.getKey();
        } catch (CosClientException | InterruptedException e) {
            e.printStackTrace();
        }

        // 拼接url返回
        return StrUtil.isBlank(cosKey) ? cosKey : Constant.COS_STORAGE_ADDRESS_PREFIX + cosKey;
    }
}
