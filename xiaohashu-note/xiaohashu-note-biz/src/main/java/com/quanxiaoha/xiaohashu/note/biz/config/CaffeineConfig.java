package com.quanxiaoha.xiaohashu.note.biz.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {
    @Bean
    public Cache<Long, String> caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(10000) //初始大小
                .maximumSize(10000)  //最大数量
                .expireAfterAccess(1, TimeUnit.HOURS) //expireAfterWrite最后一次写操作后经过指定时间过期 最后一次读或写操作后经过指定时间过期
                .build();
    }
}
