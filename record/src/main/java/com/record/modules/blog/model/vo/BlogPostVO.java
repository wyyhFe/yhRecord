package com.record.modules.blog.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.record.common.enums.BlogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客文章返回对象。
 */
@Data
@Builder
@Schema(description = "博客文章返回对象")
public class BlogPostVO {

    @Schema(description = "文章 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "作者用户 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "作者头像路径")
    private String authorAvatar;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "URL 标识")
    private String slug;

    @Schema(description = "Markdown 正文")
    private String markdownContent;

    @Schema(description = "渲染后的 HTML 正文")
    private String htmlContent;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "文章分类")
    private String category;

    @Schema(description = "标签名称列表")
    private List<String> tags;

    @Schema(description = "文章状态")
    private BlogStatus status;

    @Schema(description = "浏览人次")
    private Integer viewCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
