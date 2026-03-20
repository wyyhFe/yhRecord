package com.record.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户登录会话实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_session")
@Schema(description = "用户登录会话实体")
public class UserSession extends BaseEntity {
    @TableId
    @Schema(description = "会话 ID 主键", example = "1")
    private Long id;

    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "业务会话 ID")
    private String sessionId;

    @Schema(description = "refreshToken")
    private String refreshToken;

    @Schema(description = "refreshToken 过期时间", example = "2026-04-20 10:00:00")
    private LocalDateTime refreshExpireAt;
}
