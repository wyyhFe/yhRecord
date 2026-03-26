package com.record.modules.reminder.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 返回给前端的提醒设置对象。
 */
@Data
@Builder
@Schema(description = "提醒设置返回对象")
public class ReminderSettingVO {

    @Schema(description = "提醒设置 ID", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "是否启用日记提醒", example = "true")
    private Boolean diaryReminderEnabled;

    @Schema(description = "是否启用小程序订阅消息提醒", example = "true")
    private Boolean miniProgramReminderEnabled;

    @Schema(description = "是否启用公众号模板消息提醒", example = "false")
    private Boolean officialAccountReminderEnabled;
}
