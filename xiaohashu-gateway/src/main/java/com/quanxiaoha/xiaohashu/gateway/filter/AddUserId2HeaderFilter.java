package com.quanxiaoha.xiaohashu.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AddUserId2HeaderFilter implements GlobalFilter {
    /**
     * 请求头中，用户 ID 的键
     */
    private static final String HEADER_USER_ID = "userId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 用户 ID
        // Sa-Token默认的Token Header名字，通常是 "satoken" 或自己配置的
        String tokenValue = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (tokenValue == null) {
            // 没有token，直接放行
            return chain.filter(exchange);
        }

        Long userId = Long.valueOf(StpUtil.getLoginIdByToken(tokenValue).toString());
        log.info("## 当前登录的用户 ID: {}", userId);
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(HEADER_USER_ID, String.valueOf(userId))) // 将用户 ID 设置到请求头中
                .build();
        return chain.filter(newExchange);
    }
}
