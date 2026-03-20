package com.record.modules.reminder.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 用户提醒设置实体。
 * 一个用户只会有一条记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reminder_setting")
public class ReminderSetting extends BaseEntity {
    @TableId
    private Long id;
    /** 所属用户 ID。 */
    private Long userId;
    /** 是否开启每日写日记提醒。 */
    private Boolean diaryReminderEnabled;
    /** 每日提醒检查时间。 */
    private LocalTime diaryReminderTime;
    /** 是否启用小程序订阅消息通道。 */
    private Boolean miniProgramReminderEnabled;
    /** 是否启用公众号模板消息通道。 */
    private Boolean officialAccountReminderEnabled;
}
