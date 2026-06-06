package com.record.modules.user.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.record.common.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户绑定的第三方账号。
 */
@Data
@Builder
@Schema(description = "第三方账号绑定项")
public class UserIdentityVO {

    @Schema(description = "绑定记录 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "第三方平台", example = "GITHUB")
    private LoginType provider;

    @Schema(description = "第三方平台用户 ID")
    private String providerUserId;

    @Schema(description = "绑定时第三方平台昵称")
    private String nickname;

    @Schema(description = "绑定时第三方平台头像")
    private String avatarUrl;

    @Schema(description = "绑定时间", example = "2026-06-05T10:00:00")
    private LocalDateTime boundAt;
}
