package com.record.modules.reminder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

/**
 * 提醒设置返回对象。
 * 前端提醒设置页会用它来做表单回填。
 */
@Data
@Builder
@Schema(description = "提醒设置返回对象")
public class ReminderSettingVO {
    @Schema(description = "提醒设置 ID", example = "1")
    private Long id;
    /** 是否开启每日写日记提醒。 */
    @Schema(description = "是否开启日记提醒", example = "true")
    private Boolean diaryReminderEnabled;
    /** 触发提醒检查的时间。 */
    @Schema(description = "提醒时间", example = "21:00:00")
    private LocalTime diaryReminderTime;
    /** 是否启用小程序订阅消息发送。 */
    @Schema(description = "是否启用小程序订阅消息", example = "true")
    private Boolean miniProgramReminderEnabled;
    /** 是否启用公众号模板消息发送。 */
    @Schema(description = "是否启用公众号模板消息", example = "false")
    private Boolean officialAccountReminderEnabled;
}
