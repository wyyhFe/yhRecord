package com.record.integration.oauth;

import lombok.Builder;
import lombok.Data;

/**
 * OAuth 第三方统一用户信息。
 */
@Data
@Builder
public class OAuthUserInfo {

    /** 第三方平台的用户唯一 ID */
    private String providerUserId;

    /** 第三方平台显示名 */
    private String nickname;

    /** 第三方平台头像 URL */
    private String avatarUrl;
}
