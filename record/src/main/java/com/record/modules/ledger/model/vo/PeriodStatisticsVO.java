package com.record.modules.ledger.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 区间统计返回值。
 * 周报/月报/年报共用。
 */
@Data
@Builder
@Schema(description = "区间统计")
public class PeriodStatisticsVO {

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "统计类型：EXPENSE / INCOME")
    private String type;

    @Schema(description = "区间总额", example = "2380.50")
    private BigDecimal totalAmount;

    @Schema(description = "日均金额", example = "340.07")
    private BigDecimal dailyAverage;

    @Schema(description = "上期总额（环比用）", example = "2100.00")
    private BigDecimal previousTotal;

    @Schema(description = "收支结余（收入-支出）", example = "1620.00")
    private BigDecimal balance;

    @Schema(description = "每日金额趋势")
    private List<DailyAmountVO> dailyTrend;

    @Schema(description = "分类构成")
    private List<CategoryAmountVO> categories;
}
