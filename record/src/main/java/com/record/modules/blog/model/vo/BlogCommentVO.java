package com.record.modules.blog.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论返回对象（含子评论嵌套）。
 */
@Data
@Builder
@Schema(description = "评论返回对象")
public class BlogCommentVO {

    @Schema(description = "评论 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "评论用户 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "父评论 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "设备型号")
    private String deviceModel;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "子回复列表")
    private List<BlogCommentVO> children;
}
