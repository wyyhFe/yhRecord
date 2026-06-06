package com.record.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.LoginType;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户第三方平台绑定关系。
 * 一个 sys_user 可以有多条 identity，每条 identity 对应一个第三方账号。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_identity")
@Schema(description = "用户第三方平台绑定关系")
public class UserIdentity extends BaseEntity {

    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "关联 sys_user.id", example = "10001")
    private Long userId;

    @Schema(description = "第三方平台", example = "GITHUB")
    private LoginType provider;

    @Schema(description = "第三方平台用户唯一 ID")
    private String providerUserId;

    @Schema(description = "绑定时第三方平台昵称快照")
    private String nickname;

    @Schema(description = "绑定时第三方平台头像快照")
    private String avatarUrl;

    @Schema(description = "绑定时间", example = "2026-06-04T10:00:00")
    private LocalDateTime boundAt;
}
