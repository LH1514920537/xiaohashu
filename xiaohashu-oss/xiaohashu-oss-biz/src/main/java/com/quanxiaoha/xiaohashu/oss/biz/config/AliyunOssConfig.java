package com.quanxiaoha.xiaohashu.oss.biz.config;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunOssConfig {

    @Resource
    private AliyunOssProperties aliyunOssProperties;
    /**
     * 构建 阿里云 OSS 客户端
     *
     * @return
     */
    @Bean
    public OSS aliyunOSSClient() {
        // 设置访问凭证
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(
                aliyunOssProperties.getAccessKey(), aliyunOssProperties.getSecretKey());

        // 创建 OSSClient 实例
        return new OSSClientBuilder().build(aliyunOssProperties.getEndpoint(), credentialsProvider);
    }
}
