package com.record.modules.user.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户公开资料返回对象（用于他人查看）。
 * 不包含 openid、公众号 openid 等敏感信息。
 */
@Data
@Builder
@Schema(description = "用户公开资料返回对象")
public class PublicUserVO {

    @Schema(description = "用户 ID", example = "10001")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "昵称", example = "wyh")
    private String nickname;

    @Schema(description = "头像路径", example = "avatar/20260321/demo.jpg")
    private String avatarPath;

    @Schema(description = "个性签名", example = "把生活认真记录下来")
    private String signature;

    @Schema(description = "注册时间（加入时间）", example = "2026-03-21T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "公开日记数量", example = "10")
    private long publicDiaryCount;
}
