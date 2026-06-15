package com.record.modules.checkin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 热力图响应体。
 */
@Data
@Builder
@Schema(description = "打卡热力图")
public class HeatmapVO {

    @Schema(description = "年份", example = "2026")
    private int year;

    @Schema(description = "月份", example = "6")
    private int month;

    @Schema(description = "当前连续天数", example = "12")
    private int currentStreak;

    @Schema(description = "历史最佳连续天数", example = "18")
    private int bestStreak;

    @Schema(description = "本月打卡天数", example = "15")
    private int monthCheckinDays;

    @Schema(description = "本月总天数", example = "30")
    private int monthTotalDays;

    @Schema(description = "每日详情")
    private List<HeatmapDayVO> days;
}
