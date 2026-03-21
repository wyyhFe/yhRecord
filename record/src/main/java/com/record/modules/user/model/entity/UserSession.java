package com.record.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户登录会话。
 * 结合 Redis 记录当前生效的 sessionId，用于实现单设备登录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_session")
@Schema(description = "用户登录会话")
public class UserSession extends BaseEntity {
    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "当前会话 ID")
    private String sessionId;

    @Schema(description = "当前会话使用的 refreshToken")
    private String refreshToken;

    @Schema(description = "refreshToken 过期时间", example = "2026-04-20T10:00:00")
    private LocalDateTime refreshExpireAt;
}
