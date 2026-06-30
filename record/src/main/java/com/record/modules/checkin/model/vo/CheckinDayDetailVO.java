package com.record.modules.checkin.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 某日打卡详情（时间线用）。
 */
@Data
@Builder
@Schema(description = "某日打卡详情")
public class CheckinDayDetailVO {

    @Schema(description = "日期", example = "2026-06-30")
    private String date;

    @Schema(description = "当天打卡总次数", example = "5")
    private int totalCount;

    @Schema(description = "当天打卡任务数", example = "4")
    private int taskCount;

    @Schema(description = "打卡记录列表（按打卡时间升序）")
    private List<RecordItem> records;

    @Data
    @Builder
    @Schema(description = "单条打卡记录")
    public static class RecordItem {

        @Schema(description = "记录 ID", example = "1001")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @Schema(description = "任务 ID", example = "10")
        private Long taskId;

        @Schema(description = "任务名称", example = "20 个俯卧撑")
        private String taskName;

        @Schema(description = "任务描述")
        private String taskDescription;

        @Schema(description = "实际打卡时间", example = "2026-06-30T08:00:00")
        private LocalDateTime checkedAt;

        @Schema(description = "打卡备注")
        private String remark;

        @Schema(description = "打卡心情 Emoji", example = "😊")
        private String mood;

        @Schema(description = "附件路径列表")
        private List<String> mediaPaths;

        @Schema(description = "标签名称列表")
        private List<String> tagNames;

        @Schema(description = "标签 ID 列表")
        private List<Long> tagIds;

        @Schema(description = "是否为补卡", example = "false")
        private Boolean isMend;
    }
}
