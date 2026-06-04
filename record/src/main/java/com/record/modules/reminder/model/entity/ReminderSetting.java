package com.record.modules.reminder.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户提醒设置实体。
 * 一个用户只保留一条提醒设置记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_reminder_setting")
@Schema(description = "用户提醒设置实体")
public class ReminderSetting extends BaseEntity {

    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "是否启用日记提醒", example = "true")
    private Boolean diaryReminderEnabled;

    @Schema(description = "是否启用小程序订阅消息通道", example = "true")
    private Boolean miniProgramReminderEnabled;

    @Schema(description = "是否启用公众号模板消息通道", example = "false")
    private Boolean officialAccountReminderEnabled;
}
