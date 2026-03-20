package com.record.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小程序登录请求体。
 * code 来自前端 uni.login / wx.login。
 */
@Data
@Schema(description = "小程序登录请求体")
public class WxLoginRequest {
    /** 微信登录临时 code。 */
    @Schema(description = "微信登录临时 code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String code;
}
