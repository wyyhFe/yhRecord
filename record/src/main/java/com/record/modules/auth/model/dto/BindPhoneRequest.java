package com.record.modules.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定手机号请求。
 * code 来自小程序 button open-type="getPhoneNumber" 回调。
 */
@Data
@Schema(description = "绑定手机号请求")
public class BindPhoneRequest {

    @Schema(description = "微信手机号授权 code", requiredMode = Schema.RequiredMode.REQUIRED, example = "d912952b...")
    @NotBlank
    private String code;
}
