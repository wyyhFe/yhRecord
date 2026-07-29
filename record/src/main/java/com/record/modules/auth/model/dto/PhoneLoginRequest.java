package com.record.modules.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 手机号 + 密码登录请求。
 */
@Data
@Schema(description = "手机号 + 密码登录请求")
public class PhoneLoginRequest {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank
    private String phone;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String password;
}
