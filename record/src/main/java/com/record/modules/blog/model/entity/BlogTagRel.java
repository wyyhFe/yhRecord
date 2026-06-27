package com.record.modules.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 博客标签关联（自由文本标签，不建字典表）。
 */
@Data
@TableName("biz_blog_tag_rel")
@Schema(description = "博客标签关联")
public class BlogTagRel {

    @TableId
    @Schema(description = "关联 ID", example = "1")
    private Long id;

    @Schema(description = "文章 ID", example = "1001")
    private Long postId;

    @Schema(description = "标签名称", example = "Java")
    private String tagName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
