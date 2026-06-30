package com.record.modules.calendar.service.impl;

import com.record.modules.calendar.model.vo.CalendarDayDetailVO;
import com.record.modules.calendar.model.vo.CalendarSummaryVO;
import com.record.modules.calendar.service.CalendarService;
import com.record.modules.checkin.service.CheckinService;
import com.record.modules.diary.service.DiaryService;
import com.record.modules.memorial.service.MemorialDayService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 日历服务实现。
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

    @Override
    public CalendarDayDetailVO dayDetail(Long userId, LocalDate date) {
        return CalendarDayDetailVO.builder()
                .date(date)
                .diaries(diaryService.listByDate(userId, date))
                .checkins(checkinService.listByDate(userId, date))
                .memorialDays(memorialDayService.listByDate(userId, date))
                .build();
    }

    @Override
    public CalendarSummaryVO summaryRecent(Long userId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);

        Map<LocalDate, Long> diaryCounts = diaryService.countByDateRange(userId, start, end);
        Map<LocalDate, Long> checkinCounts = checkinService.countByDateRange(userId, start, end);
        Map<LocalDate, Long> memorialCounts = memorialDayService.countByDateRange(userId, start, end);

        List<CalendarSummaryVO.DaySummary> items = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            long diaryCount = diaryCounts.getOrDefault(d, 0L);
            long checkinCount = checkinCounts.getOrDefault(d, 0L);
            long memorialCount = memorialCounts.getOrDefault(d, 0L);

            items.add(CalendarSummaryVO.DaySummary.builder()
                    .date(d)
                    .hasDiary(diaryCount > 0)
                    .diaryCount(diaryCount)
                    .hasCheckin(checkinCount > 0)
                    .checkinCount(checkinCount)
                    .memorialCount(memorialCount)
                    .build());
        }

        return CalendarSummaryVO.builder()
                .year(0)
                .month(0)
                .days(items)
                .build();
    }

    @Override
    public long countYearlyDiaryDays(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfYear = LocalDate.of(now.getYear(), 1, 1);
        return diaryService.countByDateRange(userId, startOfYear, now).size();
    }

    @Override
    public CalendarDayDetailVO onThisDay(Long userId, LocalDate date) {
        return dayDetail(userId, date.minusYears(1));
    }
}
