package com.record.modules.diary.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日记评论返回对象。
 */
@Data
@Builder
@Schema(description = "日记评论返回对象")
public class DiaryCommentVO {

    @Schema(description = "评论 ID", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "日记 ID", example = "101")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long diaryId;

    @Schema(description = "评论用户 ID", example = "10001")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "父评论 ID，顶级评论为空", example = "10")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @Schema(description = "评论内容", example = "今天的记录很有意思")
    private String content;

    @Schema(description = "评论时间", example = "2026-03-21T21:10:00")
    private LocalDateTime createdAt;
}
