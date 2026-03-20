package com.record.modules.calendar.service.impl;

import com.record.modules.calendar.service.CalendarService;
import com.record.modules.calendar.vo.CalendarDayDetailVO;
import com.record.modules.calendar.vo.CalendarSummaryVO;
import com.record.modules.checkin.service.CheckinService;
import com.record.modules.diary.service.DiaryService;
import com.record.modules.memorial.service.MemorialDayService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 日历服务实现。
 * 负责把日记、打卡、纪念日三类数据聚合成统一的日期状态视图。
 */
@Service
public class CalendarServiceImpl implements CalendarService {

    private final DiaryService diaryService;
    private final CheckinService checkinService;
    private final MemorialDayService memorialDayService;

    public CalendarServiceImpl(DiaryService diaryService, CheckinService checkinService, MemorialDayService memorialDayService) {
        this.diaryService = diaryService;
        this.checkinService = checkinService;
        this.memorialDayService = memorialDayService;
    }

    /**
     * 按整月生成每天的摘要状态。
     */
    @Override
    public CalendarSummaryVO summary(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<CalendarSummaryVO.DaySummary> items = new ArrayList<>();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            long diaryCount = diaryService.listByDate(userId, date).size();
            long checkinCount = checkinService.listByDate(userId, date).size();
            long memorialCount = memorialDayService.listByDate(userId, date).size();
            items.add(CalendarSummaryVO.DaySummary.builder()
                    .date(date)
                    .hasDiary(diaryCount > 0)
                    .diaryCount(diaryCount)
                    .hasCheckin(checkinCount > 0)
                    .checkinCount(checkinCount)
                    .memorialCount(memorialCount)
                    .build());
        }
        return CalendarSummaryVO.builder().year(year).month(month).days(items).build();
    }

    /**
     * 某一天的详情会把三类业务数据并排返回。
     */
    @Override
    public CalendarDayDetailVO dayDetail(Long userId, LocalDate date) {
        return CalendarDayDetailVO.builder()
                .date(date)
                .diaries(diaryService.listByDate(userId, date))
                .checkins(checkinService.listByDate(userId, date))
                .memorialDays(memorialDayService.listByDate(userId, date))
                .build();
    }

    /**
     * 去年今日实际上就是对 `dayDetail` 的日期做一次减一年转换。
     */
    @Override
    public CalendarDayDetailVO onThisDay(Long userId, LocalDate date) {
        return dayDetail(userId, date.minusYears(1));
    }
}
