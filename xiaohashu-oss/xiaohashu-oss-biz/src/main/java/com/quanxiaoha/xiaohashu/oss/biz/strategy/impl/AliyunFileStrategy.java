package com.quanxiaoha.xiaohashu.oss.biz.strategy.impl;

import com.aliyun.oss.OSS;
import com.quanxiaoha.xiaohashu.oss.biz.config.AliyunOssProperties;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Slf4j
public class AliyunFileStrategy implements FileStrategy {

    @Resource
    private AliyunOssProperties aliyunOssProperties;

    @Resource
    private OSS ossClient;

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("上传文件至阿里云oss ....");
        // 判断文件是否为空
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }

        // 文件的原始名称
        String originalFileName = file.getOriginalFilename();

        // 生成存储对象的名称（将 UUID 字符串中的 - 替换成空字符串）
        String key = UUID.randomUUID().toString().replace("-", "");
        // 获取文件的后缀，如 .jpg
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));

        // 拼接上文件后缀，即为要存储的文件名
        String objectName = String.format("%s%s", key, suffix);

        log.info("==> 开始上传文件至阿里云 OSS, ObjectName: {}", objectName);

        try{
            // 上传文件至阿里云 OSS
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(file.getInputStream().readAllBytes()));

        }catch (Exception e){
            e.printStackTrace();
        }
        // 返回文件的访问链接
        String url = String.format("https://%s.%s/%s", bucketName, aliyunOssProperties.getEndpoint(), objectName);
        log.info("==> 上传文件至阿里云 OSS 成功，访问路径: {}", url);
        return url;
    }
}
