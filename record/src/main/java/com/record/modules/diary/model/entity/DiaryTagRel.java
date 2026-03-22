package com.record.modules.diary.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日记与标签关联实体。
 */
@Data
@TableName("diary_tag_rel")
@Schema(description = "日记与标签关联实体")
public class DiaryTagRel {

    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "日记 ID", example = "101")
    private Long diaryId;

    @Schema(description = "标签 ID", example = "12")
    private Long tagId;
}
