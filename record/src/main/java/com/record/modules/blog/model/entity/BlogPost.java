package com.record.modules.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.BlogStatus;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 博客文章实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_blog_post")
@Schema(description = "博客文章实体")
public class BlogPost extends BaseEntity {

    @TableId
    @Schema(description = "文章 ID", example = "1")
    private Long id;

    @Schema(description = "作者用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "文章标题", example = "Spring Boot 入门指南")
    private String title;

    @Schema(description = "URL 友好标识", example = "spring-boot-getting-started")
    private String slug;

    @Schema(description = "Markdown 正文")
    private String markdownContent;

    @Schema(description = "渲染后的 HTML 正文")
    private String htmlContent;

    @Schema(description = "摘要", example = "一篇适合新手的 Spring Boot 教程")
    private String summary;

    @Schema(description = "文章分类", example = "tech")
    private String category;

    @Schema(description = "状态", example = "PUBLISHED")
    private BlogStatus status;

    @Schema(description = "浏览人次", example = "128")
    private Integer viewCount;

    @Schema(description = "评论数", example = "5")
    private Integer commentCount;

    @Schema(description = "首次发布时间")
    private LocalDateTime publishedAt;
}
