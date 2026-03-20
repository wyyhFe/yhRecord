package com.record.modules.calendar.vo;

import com.record.modules.checkin.vo.CheckinTaskVO;
import com.record.modules.diary.vo.DiaryVO;
import com.record.modules.memorial.vo.MemorialDayVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 某一天的聚合详情。
 * 日历详情页和“去年今日”都会使用这个结构。
 */
@Data
@Builder
@Schema(description = "某一天的聚合详情")
public class CalendarDayDetailVO {
    /** 当前查看的日期。 */
    @Schema(description = "日期", example = "2026-03-21")
    private LocalDate date;
    /** 当天的日记列表。 */
    @Schema(description = "当天日记列表")
    private List<DiaryVO> diaries;
    /** 当天涉及的打卡任务列表。 */
    @Schema(description = "当天打卡任务列表")
    private List<CheckinTaskVO> checkins;
    /** 当天命中的纪念日列表。 */
    @Schema(description = "当天纪念日列表")
    private List<MemorialDayVO> memorialDays;
}
