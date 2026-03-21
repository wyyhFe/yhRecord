package com.record.modules.checkin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建打卡任务请求体。
 */
@Data
@Schema(description = "创建打卡任务请求体")
public class CreateCheckinTaskRequest {
    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "20 个俯卧撑")
    @NotBlank
    private String name;
    @Schema(description = "任务描述", example = "每天完成 20 个俯卧撑")
    private String description;
    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-21")
    @NotNull
    private LocalDate startDate;
}
