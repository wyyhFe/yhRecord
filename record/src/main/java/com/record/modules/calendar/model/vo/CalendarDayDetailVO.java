package com.record.modules.calendar.model.vo;

import com.record.modules.checkin.model.vo.CheckinTaskVO;
import com.record.modules.diary.model.vo.DiaryVO;
import com.record.modules.memorial.model.vo.MemorialDayVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 日历某一天的详情数据。
 * 用于聚合当天的日记、打卡和纪念日内容。
 */
@Data
@Builder
@Schema(description = "日历某一天的详情数据")
public class CalendarDayDetailVO {

    @Schema(description = "日期", example = "2026-03-21")
    private LocalDate date;

    @Schema(description = "当日日记列表")
    private List<DiaryVO> diaries;

    @Schema(description = "当日打卡列表")
    private List<CheckinTaskVO> checkins;

    @Schema(description = "当日纪念日列表")
    private List<MemorialDayVO> memorialDays;
}
