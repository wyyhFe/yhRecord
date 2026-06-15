package com.record.modules.checkin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 打卡请求体。
 */
@Data
@Schema(description = "打卡请求体")
public class CheckinRequest {
    @Schema(description = "打卡日期", example = "2026-03-21")
    private LocalDate checkinDate;
    @Schema(description = "备注", example = "今天也完成了")
    private String remark;
    @Schema(description = "附件列表")
    private List<CheckinMediaDTO> mediaList;
    @Schema(description = "心情 Emoji", example = "😊")
    private String mood;
    @Schema(description = "选中的标签 ID 列表")
    private List<Long> tagIds;
}
