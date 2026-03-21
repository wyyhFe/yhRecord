package com.record.modules.reminder.service;

import com.record.modules.reminder.model.dto.ReminderSettingRequest;
import com.record.modules.reminder.model.vo.ReminderSettingVO;

/**
 * 提醒服务接口。
 */
public interface ReminderService {

    /**
     * 查询当前用户的提醒设置。
     */
    ReminderSettingVO getSetting(Long userId);

    /**
     * 保存当前用户的提醒设置。
     */
    ReminderSettingVO saveSetting(Long userId, ReminderSettingRequest request);

    /**
     * 发送日记提醒。
     */
    void dispatchDiaryReminders();

    /**
     * 发送每日记账提醒。
     */
    void dispatchDailyLedgerReminders();

    /**
     * 发送记账月报提醒。
     */
    void dispatchMonthlyLedgerReminders();

    /**
     * 发送纪念日提醒。
     */
    void dispatchMemorialReminders();
}
