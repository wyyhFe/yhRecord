package com.record.modules.reminder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

/**
 * 提醒设置保存请求。
 * 由前端提醒设置页直接提交。
 */
@Data
@Schema(description = "提醒设置保存请求")
public class ReminderSettingRequest {
    /** 是否开启“当天未写日记”的提醒。 */
    @Schema(description = "是否开启日记提醒", example = "true")
    private Boolean diaryReminderEnabled;
    /** 每日检查并派发提醒的时间。 */
    @Schema(description = "每日提醒时间", example = "21:00:00")
    private LocalTime diaryReminderTime;
    /** 是否启用小程序订阅消息通道。 */
    @Schema(description = "是否启用小程序订阅消息", example = "true")
    private Boolean miniProgramReminderEnabled;
    /** 是否启用公众号模板消息通道。 */
    @Schema(description = "是否启用公众号模板消息", example = "false")
    private Boolean officialAccountReminderEnabled;
}
