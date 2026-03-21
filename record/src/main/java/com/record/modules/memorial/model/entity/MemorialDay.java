package com.record.modules.memorial.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 纪念日实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("memorial_day")
@Schema(description = "纪念日实体")
public class MemorialDay extends BaseEntity {

    @TableId
    @Schema(description = "纪念日 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "标题", example = "第一次旅行")
    private String title;

    @Schema(description = "类型", example = "LOVE")
    private String type;

    @Schema(description = "纪念日日期", example = "2026-03-21")
    private LocalDate memorialDate;

    @Schema(description = "是否每年重复", example = "true")
    private Boolean annualRepeat;

    @Schema(description = "备注", example = "第一次一起去看海")
    private String remark;

    @Schema(description = "提醒时间", example = "2026-03-21 09:00:00")
    private LocalDateTime remindAt;

    @Schema(description = "状态", example = "ENABLED")
    private CommonStatus status;
}
