package com.record.modules.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小程序扫码确认登录请求。
 */
@Data
@Schema(description = "扫码确认登录请求")
public class QrLoginRequest {

    @Schema(description = "二维码 token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String qrToken;
}
