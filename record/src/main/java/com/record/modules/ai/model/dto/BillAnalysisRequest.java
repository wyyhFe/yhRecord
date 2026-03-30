package com.record.modules.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "账单分析请求")
public class BillAnalysisRequest {

    @Schema(description = "开始日期，不传则按默认分析窗口计算", example = "2026-03-01")
    private LocalDate startDate;

    @Schema(description = "结束日期，不传则默认今天", example = "2026-03-30")
    private LocalDate endDate;

    @Schema(description = "账本 ID，不传则分析全部账本", example = "1")
    private Long bookId;

    @Schema(description = "单次参与分析的最大账单条数，不传则用系统默认值", example = "100")
    private Integer limit;

    @Schema(description = "补充分析要求", example = "重点看餐饮和通勤支出")
    private String question;
}
