package com.record.modules.blog.model.dto;

import com.record.common.enums.BlogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建 / 更新博客文章请求体。
 */
@Data
@Schema(description = "创建 / 更新博客文章请求体")
public class BlogPostRequest {

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "Spring Boot 入门指南")
    @NotBlank
    @Size(max = 256)
    private String title;

    @Schema(description = "URL 标识（留空则从标题自动生成英文 slug）", example = "spring-boot-getting-started")
    @Size(max = 256)
    private String slug;

    @Schema(description = "Markdown 正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String markdownContent;

    @Schema(description = "渲染后的 HTML 正文")
    private String htmlContent;

    @Schema(description = "摘要")
    @Size(max = 512)
    private String summary;

    @Schema(description = "文章分类", example = "tech")
    @Size(max = 64)
    private String category;

    @Schema(description = "标签名称列表", example = "[\"Java\", \"Spring\"]")
    private List<String> tags;

    @Schema(description = "文章状态", example = "PUBLISHED")
    private BlogStatus status;
}
