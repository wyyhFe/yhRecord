package com.record.modules.diary.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日记评论记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diary_comment")
@Schema(description = "日记评论记录")
public class DiaryComment extends BaseEntity {

    @TableId
    @Schema(description = "评论 ID", example = "1")
    private Long id;

    @Schema(description = "日记 ID", example = "101")
    private Long diaryId;

    @Schema(description = "评论用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "父评论 ID，顶级评论为空", example = "10")
    private Long parentId;

    @Schema(description = "评论内容", example = "今天的心情写得很真实")
    private String content;
}
