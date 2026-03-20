package com.record.modules.ledger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 年度统计返回对象。
 * 主要按标签聚合金额和占比。
 */
@Data
@Builder
@Schema(description = "年度统计返回对象")
public class YearStatisticsVO {
    @Schema(description = "统计年份", example = "2026")
    private int year;

    @Schema(description = "按标签聚合后的统计项")
    private List<TagAmountVO> items;

    /**
     * 标签维度的金额统计。
     */
    @Data
    @Builder
    @Schema(description = "标签维度金额统计")
    public static class TagAmountVO {
        @Schema(description = "标签 ID", example = "1")
        private Long tagId;

        @Schema(description = "金额", example = "356.50")
        private BigDecimal amount;

        @Schema(description = "占比", example = "0.35")
        private BigDecimal ratio;
    }
}
