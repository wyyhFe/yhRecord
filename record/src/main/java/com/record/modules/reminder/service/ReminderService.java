package com.record.modules.reminder.service;

import com.record.modules.reminder.model.dto.ReminderSettingRequest;
import com.record.modules.reminder.model.vo.ReminderSettingVO;

/**
 * 提醒服务接口。
 */
public interface ReminderService {

    /** 推送结果汇总。 */
    record DispatchResult(int success, int failed) {
        public boolean isAllFailed() { return success == 0 && failed > 0; }
        public int total() { return success + failed; }
    }

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
    DispatchResult dispatchDiaryReminders();

    /**
     * 发送每日记账提醒。
     */
    DispatchResult dispatchDailyLedgerReminders();

    /**
     * 发送记账月报提醒。
     */
    DispatchResult dispatchMonthlyLedgerReminders();

    /**
     * 发送纪念日提醒。
     */
    DispatchResult dispatchMemorialReminders();
}
