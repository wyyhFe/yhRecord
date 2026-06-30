package com.record.modules.reminder.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.reminder.model.dto.ReminderSettingRequest;
import com.record.modules.reminder.model.vo.ReminderSettingVO;
import com.record.modules.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提醒设置接口。
 */
@Tag(name = "提醒设置")
@RestController
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Operation(summary = "查询提醒设置")
    @GetMapping("/settings")
    public ApiResponse<ReminderSettingVO> settings() {
        return ApiResponse.success(reminderService.getSetting(UserContext.getUserId()));
    }

    @Operation(summary = "保存提醒设置")
    @PutMapping("/settings")
    public ApiResponse<ReminderSettingVO> save(@RequestBody ReminderSettingRequest request) {
        return ApiResponse.success(reminderService.saveSetting(UserContext.getUserId(), request));
    }

    // ==================== 手动触发提醒（供小程序用户调试/测试用） ====================

    @Operation(summary = "手动触发日记提醒")
    @PostMapping("/trigger/diary")
    public ApiResponse<ReminderService.DispatchResult> triggerDiary() {
        ReminderService.DispatchResult result = reminderService.dispatchDiaryReminders();
        if (result.isAllFailed()) {
            return ApiResponse.failure(500, "日记提醒全部发送失败: success=" + result.success() + ", failed=" + result.failed());
        }
        return ApiResponse.success(result);
    }

    @Operation(summary = "手动触发每日记账提醒")
    @PostMapping("/trigger/ledger")
    public ApiResponse<ReminderService.DispatchResult> triggerLedger() {
        ReminderService.DispatchResult result = reminderService.dispatchDailyLedgerReminders();
        if (result.isAllFailed()) {
            return ApiResponse.failure(500, "记账提醒全部发送失败: success=" + result.success() + ", failed=" + result.failed());
        }
        return ApiResponse.success(result);
    }

    @Operation(summary = "手动触发记账月报提醒")
    @PostMapping("/trigger/monthly")
    public ApiResponse<ReminderService.DispatchResult> triggerMonthly() {
        ReminderService.DispatchResult result = reminderService.dispatchMonthlyLedgerReminders();
        if (result.isAllFailed()) {
            return ApiResponse.failure(500, "记账月报全部发送失败: success=" + result.success() + ", failed=" + result.failed());
        }
        return ApiResponse.success(result);
    }

    @Operation(summary = "手动触发纪念日提醒")
    @PostMapping("/trigger/memorial")
    public ApiResponse<ReminderService.DispatchResult> triggerMemorial() {
        ReminderService.DispatchResult result = reminderService.dispatchMemorialReminders();
        if (result.isAllFailed()) {
            return ApiResponse.failure(500, "纪念日提醒全部发送失败: success=" + result.success() + ", failed=" + result.failed());
        }
        return ApiResponse.success(result);
    }
}
