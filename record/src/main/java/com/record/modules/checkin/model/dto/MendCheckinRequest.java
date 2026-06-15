package com.record.modules.checkin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 补卡请求体。
 */
@Data
@Schema(description = "补卡请求体")
public class MendCheckinRequest {

    @Schema(description = "任务 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull
    private Long taskId;

    @Schema(description = "补卡日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-06-14")
    @NotNull
    private LocalDate mendDate;
}
