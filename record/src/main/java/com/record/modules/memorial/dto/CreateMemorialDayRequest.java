package com.record.modules.memorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 创建纪念日请求体。
 */
@Data
@Schema(description = "创建纪念日请求体")
public class CreateMemorialDayRequest {
    @Schema(description = "纪念日标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "和她第一次见面")
    @NotBlank
    private String title;

    @Schema(description = "纪念日类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOVE")
    @NotBlank
    private String type;

    @Schema(description = "纪念日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-21")
    @NotNull
    private LocalDate memorialDate;

    @Schema(description = "是否每年重复", example = "true")
    private Boolean annualRepeat;

    @Schema(description = "备注", example = "第一次一起看海")
    private String remark;

    @Schema(description = "提醒时间", example = "2026-03-21 09:00:00")
    private LocalDateTime remindAt;
}
