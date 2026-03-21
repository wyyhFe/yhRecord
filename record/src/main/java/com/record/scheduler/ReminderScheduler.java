package com.record.scheduler;

import com.record.modules.reminder.service.ReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 提醒任务调度器。
 * `@Scheduled` 表示方法会按照指定的 cron 表达式自动执行。
 */
@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 每天晚上 22:00 执行一次日记提醒检查。
     * 只有当天还没有写日记的用户才会收到提醒。
     * `0 0 22 * * ?` 表示每天 22:00:00 执行一次。
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void diaryReminder() {
        reminderService.dispatchDiaryReminders();
    }

    /**
     * 每天晚上 20:30 发送每日记账提醒。
     * 只有当天还没有记账的用户才会收到提醒。
     * `0 30 20 * * ?` 表示每天 20:30:00 执行一次。
     */
    @Scheduled(cron = "0 30 20 * * ?")
    public void dailyLedgerReminder() {
        reminderService.dispatchDailyLedgerReminders();
    }

    /**
     * 每月最后一天晚上 21:00 发送记账月报。
     * 先按每天 21:00 进入方法，再判断当天是否为当月最后一天。
     */
    @Scheduled(cron = "0 0 21 * * ?")
    public void monthlyLedgerReminder() {
        LocalDate today = LocalDate.now();
        if (today.equals(today.withDayOfMonth(today.lengthOfMonth()))) {
            reminderService.dispatchMonthlyLedgerReminders();
        }
    }

    /**
     * 每天早上 08:05 检查纪念日提醒。
     * `0 5 8 * * ?` 表示每天 08:05:00 执行一次。
     */
    @Scheduled(cron = "0 5 8 * * ?")
    public void memorialReminder() {
        reminderService.dispatchMemorialReminders();
    }
}
