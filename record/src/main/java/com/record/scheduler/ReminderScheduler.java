package com.record.scheduler;

import com.record.modules.reminder.service.ReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "0 * * * * ?")
    public void diaryReminder() {
        reminderService.dispatchDiaryReminders();
    }

    @Scheduled(cron = "0 5 8 * * ?")
    public void memorialReminder() {
        reminderService.dispatchMemorialReminders();
    }
}
