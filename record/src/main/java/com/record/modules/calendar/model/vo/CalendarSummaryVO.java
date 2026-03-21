package com.record.modules.calendar.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 月历摘要返回对象。
 */
@Data
@Builder
@Schema(description = "月历摘要返回对象")
public class CalendarSummaryVO {

    @Schema(description = "年份", example = "2026")
    private int year;

    @Schema(description = "月份", example = "3")
    private int month;

    @Schema(description = "当月每天的状态摘要")
    private List<DaySummary> days;

    /**
     * 单日状态摘要。
     */
    @Data
    @Builder
    @Schema(description = "单日状态摘要")
    public static class DaySummary {

        @Schema(description = "日期", example = "2026-03-21")
        private LocalDate date;

        @Schema(description = "当天是否有日记", example = "true")
        private boolean hasDiary;

        @Schema(description = "当日日记数量", example = "1")
        private long diaryCount;

        @Schema(description = "当天是否有打卡", example = "false")
        private boolean hasCheckin;

        @Schema(description = "当日打卡数量", example = "0")
        private long checkinCount;

        @Schema(description = "当日纪念日数量", example = "1")
        private long memorialCount;
    }
}
