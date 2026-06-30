package com.record.modules.calendar.service;

import com.record.modules.calendar.model.vo.CalendarDayDetailVO;
import com.record.modules.calendar.model.vo.CalendarSummaryVO;

import java.time.LocalDate;

public interface CalendarService {
    CalendarSummaryVO summary(Long userId, int year, int month);

    CalendarDayDetailVO dayDetail(Long userId, LocalDate date);

    CalendarDayDetailVO onThisDay(Long userId, LocalDate date);

    /**
     * 获取今年有日记记录的天数。
     */
    long countYearlyDiaryDays(Long userId);

    /**
     * 查询最近 N 天的日历摘要。
     * 后端只查需要的数据，比按月全量查询高效。
     *
     * @param userId 用户ID
     * @param days   最近天数（如 7 表示最近7天）
     * @return 日历摘要（不含 year/month，或置0）
     */
    CalendarSummaryVO summaryRecent(Long userId, int days);
}
