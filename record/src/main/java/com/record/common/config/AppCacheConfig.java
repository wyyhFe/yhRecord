package com.record.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置（Caffeine）。
 * <p>
 * 用于字典类数据（用户、标签模板等高频读取、低频变更的数据）的本地缓存，
 * 搭配 Redis 做二级缓存或穿透保护。当前仅使用 Caffeine 单层本地缓存，
 * 适合单实例部署；多实例场景建议改为 Redis 集中缓存。
 * </p>
 */
@Configuration
@EnableCaching
public class AppCacheConfig {

    /**
     * 默认缓存管理器：缓存条目写入后 10 分钟过期，最大 1000 条。
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        // 允许缓存名动态创建（按需懒加载），无需提前声明所有 cacheNames。
        // 注意：Spring Framework 6.2+ 移除了 setDynamic() 方法。
        // 使用无参构造（已在上方使用）即默认进入动态模式；
        // 若调用 setCacheNames(集合) 才会切换为静态模式。
        // 此处无需额外调用，动态模式已生效。
        return cacheManager;
    }
}
