package com.record.modules.calendar.service;

import com.record.modules.calendar.model.vo.CalendarDayDetailVO;
import com.record.modules.calendar.model.vo.CalendarSummaryVO;

import java.time.LocalDate;

public interface CalendarService {
    CalendarSummaryVO summary(Long userId, int year, int month);
    CalendarDayDetailVO dayDetail(Long userId, LocalDate date);
    CalendarDayDetailVO onThisDay(Long userId, LocalDate date);
}

