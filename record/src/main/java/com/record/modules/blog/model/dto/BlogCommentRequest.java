package com.record.modules.blog.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 博客评论请求体。
 */
@Data
@Schema(description = "博客评论请求体")
public class BlogCommentRequest {

    @Schema(description = "目标类型：BLOG_POST / DIARY", requiredMode = Schema.RequiredMode.REQUIRED, example = "BLOG_POST")
    @NotBlank
    private String targetType;

    @Schema(description = "目标 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull
    private Long targetId;

    @Schema(description = "父评论 ID（回复评论时传入，顶级评论不传）", example = "10")
    private Long parentId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "写得很好！")
    @NotBlank
    @Size(max = 1000)
    private String content;

    @Schema(description = "设备型号", example = "iPhone 15 Pro")
    private String deviceModel;

    @Schema(description = "平台来源", example = "WEB")
    private String platform;
}
