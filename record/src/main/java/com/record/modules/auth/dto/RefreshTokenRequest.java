package com.record.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 token 请求体。
 * 当前仅需要前端传 refreshToken。
 */
@Data
@Schema(description = "刷新 token 请求体")
public class RefreshTokenRequest {
    /** 用于换取新 accessToken 的刷新令牌。 */
    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String refreshToken;
}
