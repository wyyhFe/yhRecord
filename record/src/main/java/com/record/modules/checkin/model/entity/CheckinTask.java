package com.record.modules.checkin.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 打卡任务实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin_task")
@Schema(description = "打卡任务实体")
public class CheckinTask extends BaseEntity {

    @TableId
    @Schema(description = "任务 ID", example = "10")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "任务名称", example = "20 个俯卧撑")
    private String name;

    @Schema(description = "任务描述", example = "每天完成 20 个俯卧撑")
    private String description;

    @Schema(description = "开始日期", example = "2026-03-21")
    private LocalDate startDate;

    @Schema(description = "状态", example = "ENABLED")
    private CommonStatus status;
}
