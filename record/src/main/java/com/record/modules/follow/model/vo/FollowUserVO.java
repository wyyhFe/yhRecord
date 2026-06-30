package com.record.modules.follow.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "关注/粉丝用户信息")
public class FollowUserVO {

    @Schema(description = "用户 ID", example = "10002")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像路径")
    private String avatarPath;

    @Schema(description = "关注时间")
    private LocalDateTime followedAt;
}
