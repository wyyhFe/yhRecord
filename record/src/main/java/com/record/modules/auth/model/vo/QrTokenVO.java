package com.record.modules.auth.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 二维码扫码登录 token 响应。
 */
@Data
@Builder
@Schema(description = "二维码扫码登录 token")
public class QrTokenVO {

    @Schema(description = "二维码 token", example = "a1b2c3d4-...")
    private String qrToken;

    @Schema(description = "有效期（秒）", example = "120")
    private long expireSeconds;
}
