package com.record.modules.diary.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日记附件实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_diary_media")
@Schema(description = "日记附件实体")
public class DiaryMedia extends BaseEntity {

    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "日记 ID", example = "101")
    private Long diaryId;

    @Schema(description = "附件类型", example = "IMAGE")
    private String mediaType;

    @Schema(description = "OSS 相对路径", example = "diary/20260321/demo.jpg")
    private String filePath;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
}
