package com.record.modules.reminder.service;

import com.record.modules.reminder.dto.ReminderSettingRequest;
import com.record.modules.reminder.vo.ReminderSettingVO;

public interface ReminderService {
    ReminderSettingVO getSetting(Long userId);
    ReminderSettingVO saveSetting(Long userId, ReminderSettingRequest request);
    void dispatchDiaryReminders();
    void dispatchMemorialReminders();
}
