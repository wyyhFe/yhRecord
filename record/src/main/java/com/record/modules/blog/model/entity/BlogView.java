package com.record.modules.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 博客浏览记录（用于 IP + 用户去重计数）。
 */
@Data
@TableName("biz_blog_view")
@Schema(description = "博客浏览记录")
public class BlogView {

    @TableId
    @Schema(description = "记录 ID", example = "1")
    private Long id;

    @Schema(description = "文章 ID", example = "1001")
    private Long postId;

    @Schema(description = "浏览用户 ID（匿名浏览为空）")
    private Long userId;

    @Schema(description = "浏览者 IP 地址")
    private String viewerIp;

    @Schema(description = "浏览时间")
    private LocalDateTime viewedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
