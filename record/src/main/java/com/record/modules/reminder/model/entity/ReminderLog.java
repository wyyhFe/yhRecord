package com.record.modules.reminder.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.ReminderBusinessType;
import com.record.common.enums.ReminderChannel;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 提醒发送日志实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_reminder_log")
@Schema(description = "提醒发送日志实体")
public class ReminderLog extends BaseEntity {

    @TableId
    @Schema(description = "日志 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "业务类型", example = "DIARY_DAILY")
    private ReminderBusinessType businessType;

    @Schema(description = "发送通道", example = "MINI_PROGRAM")
    private ReminderChannel channel;

    @Schema(description = "业务目标 ID", example = "10")
    private Long targetId;

    @Schema(description = "业务日期", example = "2026-03-21")
    private LocalDate businessDate;

    @Schema(description = "发送状态", example = "SUCCESS")
    private String sendStatus;

    @Schema(description = "发送结果说明", example = "发送成功")
    private String sendMessage;

    @Schema(description = "发送时间", example = "2026-03-21T21:00:00")
    private LocalDateTime sentAt;
}
