package com.record.modules.reminder.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 保存提醒设置时使用的请求体。
 * 全局日记提醒已统一固定为每天 22:00，这里只保留开关字段。
 */
@Data
@Schema(description = "提醒设置请求体")
public class ReminderSettingRequest {

    @Schema(description = "是否启用日记提醒", example = "true")
    private Boolean diaryReminderEnabled;

    @Schema(description = "是否启用小程序订阅消息提醒", example = "true")
    private Boolean miniProgramReminderEnabled;

    @Schema(description = "是否启用公众号模板消息提醒", example = "false")
    private Boolean officialAccountReminderEnabled;
}
