package com.record.common.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Lettuce 客户端定制。
 * <p>主要目的是给跨公网连远端 Redis 的开发场景启用 TCP keepalive，
 * 避免 NAT/防火墙把 idle 连接静默回收后留下"看起来活、实际死"的僵尸连接，
 * 让客户端每条命令都要等满命令 timeout 才报错。</p>
 */
@Configuration
public class RedisLettuceConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        SocketOptions socketOptions = SocketOptions.builder()
                .keepAlive(true)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                // 连接断开时直接拒绝命令，避免命令在死连接上排队等 timeout
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .build();

        return builder -> builder.clientOptions(clientOptions);
    }
}
