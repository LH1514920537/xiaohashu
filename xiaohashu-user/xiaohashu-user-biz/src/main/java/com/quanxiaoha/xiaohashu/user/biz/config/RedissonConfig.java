package com.quanxiaoha.xiaohashu.user.biz.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")   // 这里换成你的 Redis 地址
                .setDatabase(0)
                .setPassword("950088");

        return Redisson.create(config);
    }
}
