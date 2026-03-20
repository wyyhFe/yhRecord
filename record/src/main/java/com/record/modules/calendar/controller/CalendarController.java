package com.record.modules.calendar.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.calendar.service.CalendarService;
import com.record.modules.calendar.vo.CalendarDayDetailVO;
import com.record.modules.calendar.vo.CalendarSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 日历与回忆接口。
 * 主要服务于月历状态展示、某天详情展示和“去年今日”查询。
 */
@Tag(name = "日历")
@RestController
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * 获取指定年月的日期摘要。
     * 返回每天是否写了日记、是否完成打卡、纪念日数量等信息。
     */
    @Operation(summary = "月历摘要")
    @GetMapping("/calendar/summary")
    public ApiResponse<CalendarSummaryVO> summary(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(calendarService.summary(UserContext.getUserId(), year, month));
    }

    /**
     * 获取某一天的详细内容。
     * 会聚合当日日记、打卡和纪念日信息。
     */
    @Operation(summary = "某日详情")
    @GetMapping("/calendar/day-detail")
    public ApiResponse<CalendarDayDetailVO> dayDetail(@RequestParam LocalDate date) {
        return ApiResponse.success(calendarService.dayDetail(UserContext.getUserId(), date));
    }

    /**
     * 获取“去年今日”的内容。
     * 传入一个日期，后端会查找上一年同月同日的历史记录。
     */
    @Operation(summary = "去年今日")
    @GetMapping("/memories/on-this-day")
    public ApiResponse<CalendarDayDetailVO> onThisDay(@RequestParam LocalDate date) {
        return ApiResponse.success(calendarService.onThisDay(UserContext.getUserId(), date));
    }
}
