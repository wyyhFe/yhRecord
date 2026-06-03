package com.record.integration.oauth;

import com.record.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OAuth 提供商注册中心。
 * 启动时自动收集所有 OAuthProvider 实现，按 providerName 路由。
 */
@Component
public class OAuthProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(OAuthProviderRegistry.class);

    private final Map<String, OAuthProvider> providers;

    public OAuthProviderRegistry(List<OAuthProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        OAuthProvider::getProviderName,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("[OAuth] 重复的 providerName={}，使用后者覆盖", existing.getProviderName());
                            return replacement;
                        }
                ));
        log.info("[OAuth] 已注册的提供商: {}", providers.keySet());
    }

    /**
     * 获取指定提供商。
     *
     * @param providerName 提供商名称（github / google）
     * @return 对应的 OAuthProvider
     * @throws BusinessException 找不到时抛出
     */
    public OAuthProvider getProvider(String providerName) {
        OAuthProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new BusinessException("不支持的登录方式: " + providerName);
        }
        return provider;
    }

    /** 获取所有已注册的提供商名称。 */
    public List<String> getProviderNames() {
        return List.copyOf(providers.keySet());
    }
}
