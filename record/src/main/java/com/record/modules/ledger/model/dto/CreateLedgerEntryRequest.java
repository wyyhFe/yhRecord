package com.record.modules.ledger.model.dto;

import com.record.common.enums.LedgerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建记账流水请求体。
 */
@Data
@Schema(description = "创建记账流水请求体")
public class CreateLedgerEntryRequest {

    @Schema(description = "账本 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    private Long bookId;

    @Schema(description = "收支类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "EXPENSE")
    @NotNull
    private LedgerType type;

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "52.30")
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    @Schema(description = "记账日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-21")
    @NotNull
    private LocalDate entryDate;

    @Schema(description = "备注", example = "午餐")
    private String remark;

    @Schema(description = "图片 OSS 路径", example = "ledger/20260321/demo.jpg")
    private String imagePath;

    @Schema(description = "标签 ID 列表")
    private List<Long> tagIds;
}
