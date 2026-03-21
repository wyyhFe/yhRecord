package com.record.modules.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求。
 * 登录凭证来自 uni.login 或 wx.login。
 */
@Data
@Schema(description = "微信小程序登录请求")
public class WxLoginRequest {

    @Schema(description = "小程序登录 code", requiredMode = Schema.RequiredMode.REQUIRED, example = "081xYw0000abcdEFGH")
    @NotBlank
    private String code;
}
