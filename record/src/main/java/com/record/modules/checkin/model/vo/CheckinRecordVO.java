package com.record.modules.checkin.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 打卡记录返回对象（管理后台分页列表专用）。
 */
@Data
@Builder
@Schema(description = "打卡记录返回对象")
public class CheckinRecordVO {

    @Schema(description = "记录 ID", example = "1001")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "任务 ID", example = "10")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    @Schema(description = "任务名称", example = "20 个俯卧撑")
    private String taskName;

    @Schema(description = "打卡日期", example = "2026-06-23")
    private LocalDate checkinDate;

    @Schema(description = "打卡备注")
    private String remark;

    @Schema(description = "打卡心情 Emoji", example = "😊")
    private String mood;

    @Schema(description = "附件路径列表")
    private List<String> mediaPaths;

    @Schema(description = "标签名称列表")
    private List<String> tagNames;

    @Schema(description = "是否为补卡", example = "false")
    private Boolean isMend;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
