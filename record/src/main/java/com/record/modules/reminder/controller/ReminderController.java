package com.record.modules.reminder.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.reminder.dto.ReminderSettingRequest;
import com.record.modules.reminder.service.ReminderService;
import com.record.modules.reminder.vo.ReminderSettingVO;
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
@Tag(name = "提醒")
@RestController
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 获取当前登录用户的提醒设置。
     */
    @Operation(summary = "获取提醒设置")
    @GetMapping("/settings")
    public ApiResponse<ReminderSettingVO> settings() {
        return ApiResponse.success(reminderService.getSetting(UserContext.getUserId()));
    }

    /**
     * 保存当前登录用户的提醒设置。
     */
    @Operation(summary = "保存提醒设置")
    @PutMapping("/settings")
    public ApiResponse<ReminderSettingVO> save(@RequestBody ReminderSettingRequest request) {
        return ApiResponse.success(reminderService.saveSetting(UserContext.getUserId(), request));
    }
}
