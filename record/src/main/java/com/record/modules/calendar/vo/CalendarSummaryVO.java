package com.record.modules.calendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 月历摘要返回对象。
 * 用于前端月历组件按天渲染状态。
 */
@Data
@Builder
@Schema(description = "月历摘要返回对象")
public class CalendarSummaryVO {
    /** 年份。 */
    @Schema(description = "年份", example = "2026")
    private int year;
    /** 月份。 */
    @Schema(description = "月份", example = "3")
    private int month;
    /** 当月每天的状态摘要。 */
    @Schema(description = "当月每天的状态摘要")
    private List<DaySummary> days;

    /**
     * 某一天的状态摘要。
     */
    @Data
    @Builder
    @Schema(description = "单日状态摘要")
    public static class DaySummary {
        /** 对应日期。 */
        @Schema(description = "日期", example = "2026-03-21")
        private LocalDate date;
        /** 当天是否写了日记。 */
        @Schema(description = "当天是否写了日记", example = "true")
        private boolean hasDiary;
        /** 当天日记数量。 */
        @Schema(description = "当天日记数量", example = "1")
        private long diaryCount;
        /** 当天是否完成过打卡。 */
        @Schema(description = "当天是否完成过打卡", example = "false")
        private boolean hasCheckin;
        /** 当天打卡次数。 */
        @Schema(description = "当天打卡次数", example = "0")
        private long checkinCount;
        /** 当天命中的纪念日数量。 */
        @Schema(description = "当天命中的纪念日数量", example = "1")
        private long memorialCount;
    }
}
