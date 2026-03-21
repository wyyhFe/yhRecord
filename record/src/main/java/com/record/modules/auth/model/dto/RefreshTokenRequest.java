package com.record.modules.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 token 请求体。
 */
@Data
@Schema(description = "刷新 token 请求体")
public class RefreshTokenRequest {
    @Schema(description = "refreshToken", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String refreshToken;
}
