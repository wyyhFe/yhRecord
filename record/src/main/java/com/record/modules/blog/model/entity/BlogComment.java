package com.record.modules.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用评论实体（博客文章 + 日记）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_comment")
@Schema(description = "通用评论记录")
public class BlogComment extends BaseEntity {

    @TableId
    @Schema(description = "评论 ID", example = "1")
    private Long id;

    @Schema(description = "目标类型", example = "BLOG_POST")
    private String targetType;

    @Schema(description = "目标 ID", example = "1001")
    private Long targetId;

    @Schema(description = "评论用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "父评论 ID，顶级评论为空", example = "10")
    private Long parentId;

    @Schema(description = "评论内容", example = "写得很好，受益匪浅")
    private String content;

    @Schema(description = "设备型号", example = "iPhone 15 Pro")
    private String deviceModel;

    @Schema(description = "平台来源", example = "WEB")
    private String platform;
}
