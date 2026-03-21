package com.record.modules.calendar.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.calendar.model.vo.CalendarDayDetailVO;
import com.record.modules.calendar.model.vo.CalendarSummaryVO;
import com.record.modules.calendar.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 日历与回忆相关接口。
 */
@Tag(name = "日历与回忆")
@RestController
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Operation(summary = "查询月度日历摘要")
    @GetMapping("/calendar/summary")
    public ApiResponse<CalendarSummaryVO> summary(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(calendarService.summary(UserContext.getUserId(), year, month));
    }

    @Operation(summary = "查询某日详情")
    @GetMapping("/calendar/day-detail")
    public ApiResponse<CalendarDayDetailVO> dayDetail(@RequestParam LocalDate date) {
        return ApiResponse.success(calendarService.dayDetail(UserContext.getUserId(), date));
    }

    @Operation(summary = "查询去年今日")
    @GetMapping("/memories/on-this-day")
    public ApiResponse<CalendarDayDetailVO> onThisDay(@RequestParam LocalDate date) {
        return ApiResponse.success(calendarService.onThisDay(UserContext.getUserId(), date));
    }
}
