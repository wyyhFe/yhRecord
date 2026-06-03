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
}

