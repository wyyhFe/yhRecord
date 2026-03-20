package com.record.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录成功后的会话返回对象。
 * 前端会把这里的 token 和 sessionId 存到本地。
 */
@Data
@Builder
@Schema(description = "登录成功后的会话信息")
public class AuthTokenVO {
    /** 当前登录用户 ID。 */
    @Schema(description = "当前登录用户 ID", example = "10001")
    private Long userId;
    /** 小程序 openid。 */
    @Schema(description = "小程序 openid")
    private String openid;
    /** 短期 accessToken。 */
    @Schema(description = "accessToken")
    private String accessToken;
    /** 长期 refreshToken。 */
    @Schema(description = "refreshToken")
    private String refreshToken;
    /** 当前单设备会话 ID。 */
    @Schema(description = "单设备会话 ID")
    private String sessionId;
}
