package com.record.modules.checkin.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录。
 * 同一任务、同一用户、同一日期只允许存在一条有效记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin_record")
@Schema(description = "打卡记录")
public class CheckinRecord extends BaseEntity {
    @TableId
    @Schema(description = "打卡记录 ID", example = "1")
    private Long id;

    @Schema(description = "任务 ID", example = "10")
    private Long taskId;

    @Schema(description = "打卡用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "打卡业务日期", example = "2026-03-21")
    private LocalDate checkinDate;

    @Schema(description = "实际打卡时间", example = "2026-03-21T08:00:00")
    private LocalDateTime checkedAt;

    @Schema(description = "打卡备注", example = "今天完成 20 个俯卧撑")
    private String remark;
}
