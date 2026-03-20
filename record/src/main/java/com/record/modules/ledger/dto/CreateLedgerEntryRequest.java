package com.record.modules.ledger.dto;

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
    /** 所属账本 ID。 */
    @Schema(description = "所属账本 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    private Long bookId;

    /** 收支类型。 */
    @Schema(description = "收支类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "EXPENSE")
    @NotNull
    private LedgerType type;

    /** 金额，最小值不低于 0。 */
    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "52.30")
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    /** 记账日期。 */
    @Schema(description = "记账日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-21")
    @NotNull
    private LocalDate entryDate;

    /** 备注。 */
    @Schema(description = "备注", example = "午饭和咖啡")
    private String remark;

    /** 图片路径。 */
    @Schema(description = "记账图片 OSS 相对路径", example = "ledger/20260321/demo.jpg")
    private String imagePath;

    /** 关联标签 ID 列表。 */
    @Schema(description = "关联标签 ID 列表")
    private List<Long> tagIds;
}
