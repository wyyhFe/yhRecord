package com.record.modules.checkin.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡任务返回对象。
 * 用于任务列表、日历详情等展示场景。
 */
@Data
@Builder
@Schema(description = "打卡任务返回对象")
public class CheckinTaskVO {

    @Schema(description = "任务 ID", example = "10")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "任务名称", example = "20 个俯卧撑")
    private String name;

    @Schema(description = "任务描述", example = "每天完成 20 个俯卧撑")
    private String description;

    @Schema(description = "开始日期", example = "2026-03-21")
    private LocalDate startDate;

    @Schema(description = "累计打卡次数", example = "12")
    private long totalCount;

    @Schema(description = "最近一次打卡时间", example = "2026-03-21T08:00:00")
    private LocalDateTime latestCheckedAt;
}
