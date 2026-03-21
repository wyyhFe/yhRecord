package com.record.modules.checkin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

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
}
