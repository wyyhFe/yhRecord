package com.record.modules.auth.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * OAuth 授权 URL 返回结构。前端拿到后自行 window.location 跳转。
 */
@Data
@Builder
@Schema(description = "OAuth 授权跳转地址")
public class OAuthAuthorizeUrlVO {

    @Schema(description = "完整的第三方授权 URL")
    private String authorizeUrl;
}
