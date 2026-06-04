package com.record.modules.diary.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日记点赞记录。
 * 同一用户对同一篇日记只允许点赞一次。
 */
@Data
@TableName("biz_diary_like")
@Schema(description = "日记点赞记录")
public class DiaryLike {

    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "日记 ID", example = "101")
    private Long diaryId;

    @Schema(description = "点赞用户 ID", example = "10001")
    private Long userId;
}
