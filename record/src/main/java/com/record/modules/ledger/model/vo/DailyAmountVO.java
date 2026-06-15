package com.record.modules.ledger.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日金额（趋势图用）。
 */
@Data
@Builder
@Schema(description = "每日金额")
public class DailyAmountVO {

    @Schema(description = "日期", example = "2026-06-15")
    private LocalDate date;

    @Schema(description = "金额", example = "356.50")
    private BigDecimal amount;
}
