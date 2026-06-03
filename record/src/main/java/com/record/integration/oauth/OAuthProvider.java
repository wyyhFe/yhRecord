package com.record.integration.oauth;

/**
 * OAuth 提供商策略接口。
 * 后续新增登录方式只需实现此接口并注册到 OAuthProviderRegistry。
 */
public interface OAuthProvider {

    /** 提供商名称，如 github、google */
    String getProviderName();

    /**
     * 构建授权 URL（用户点击后浏览器跳转）。
     *
     * @param state 防 CSRF 的随机状态码
     * @return 完整的授权 URL
     */
    String buildAuthorizationUrl(String state);

    /**
     * 用授权码换取第三方用户信息。
     *
     * @param code  授权码
     * @return 第三方用户信息
     */
    OAuthUserInfo handleCallback(String code);
}
