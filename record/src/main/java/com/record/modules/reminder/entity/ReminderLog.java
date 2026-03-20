package com.record.modules.reminder.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.ReminderBusinessType;
import com.record.common.enums.ReminderChannel;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 提醒发送日志。
 * 主要用于幂等控制、失败排查和后续运营统计。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reminder_log")
public class ReminderLog extends BaseEntity {
    @TableId
    private Long id;
    /** 收到提醒的用户 ID。 */
    private Long userId;
    /** 提醒属于哪类业务。 */
    private ReminderBusinessType businessType;
    /** 通过哪个通道发送。 */
    private ReminderChannel channel;
    /** 关联业务对象 ID，例如纪念日 ID。 */
    private Long targetId;
    /** 业务日期，用于限制同一天重复发送。 */
    private LocalDate businessDate;
    /** 发送状态，例如 SUCCESS / FAILED。 */
    private String sendStatus;
    /** 发送结果说明或失败原因。 */
    private String sendMessage;
    /** 实际发送时间。 */
    private LocalDateTime sentAt;
}
