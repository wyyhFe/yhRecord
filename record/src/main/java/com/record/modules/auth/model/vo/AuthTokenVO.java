package com.record.modules.auth.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录成功后的令牌信息。
 */
@Data
@Builder
@Schema(description = "登录成功后的令牌信息")
public class AuthTokenVO {

    @Schema(description = "用户 ID", example = "10001")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "当前登录用户的小程序 openid", example = "o1234567890")
    private String openid;

    @Schema(description = "短期有效的访问令牌")
    private String accessToken;

    @Schema(description = "用于续签 accessToken 的刷新令牌")
    private String refreshToken;

    @Schema(description = "当前登录会话 ID")
    private String sessionId;
}
