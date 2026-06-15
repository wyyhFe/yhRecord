package com.record.modules.checkin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 热力图每日详情。
 */
@Data
@Builder
@Schema(description = "热力图每日详情")
public class HeatmapDayVO {

    @Schema(description = "日期", example = "2026-06-15")
    private LocalDate date;

    @Schema(description = "该日启用的任务数", example = "4")
    private int totalTasks;

    @Schema(description = "该日完成的任务数", example = "3")
    private int completedTasks;
}
