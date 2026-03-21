package com.record.modules.reminder.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.reminder.model.dto.ReminderSettingRequest;
import com.record.modules.reminder.model.vo.ReminderSettingVO;
import com.record.modules.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
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
}
