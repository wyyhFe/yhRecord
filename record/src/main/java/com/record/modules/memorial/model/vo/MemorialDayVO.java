package com.record.modules.memorial.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 纪念日返回对象。
 */
@Data
@Builder
@Schema(description = "纪念日返回对象")
public class MemorialDayVO {

    @Schema(description = "纪念日 ID", example = "1")
    private Long id;

    @Schema(description = "标题", example = "第一次旅行")
    private String title;

    @Schema(description = "类型", example = "LOVE")
    private String type;

    @Schema(description = "纪念日期", example = "2026-03-21")
    private LocalDate memorialDate;

    @Schema(description = "是否每年重复", example = "true")
    private Boolean annualRepeat;

    @Schema(description = "备注", example = "第一次一起去看海")
    private String remark;

    @Schema(description = "提醒时间", example = "2026-03-21T09:00:00")
    private LocalDateTime remindAt;
}
