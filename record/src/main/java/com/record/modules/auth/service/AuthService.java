package com.record.modules.auth.service;

import com.record.modules.auth.model.vo.AuthTokenVO;

public interface AuthService {
    AuthTokenVO wxLogin(String code);
    AuthTokenVO refreshToken(String refreshToken);
    void logout(Long userId);

    /**
     * 构建指定 OAuth 提供商的授权 URL。
     */
    String buildOAuthAuthorizeUrl(String provider, String state);

    /**
     * 处理 OAuth 回调：用 code 换取用户信息并签发令牌。
     */
    AuthTokenVO oauthLogin(String provider, String code);

    /**
     * 已登录态下，把当前 OAuth 回调里的第三方账号绑定到指定用户。
     * 不签发新 token，只写 sys_user_identity。
     *
     * @param userId   当前登录用户 ID（从 state 上下文取出）
     * @param provider 第三方平台名（github / google 等）
     * @param code     回调返回的授权码
     */
    void bindIdentity(Long userId, String provider, String code);
}

