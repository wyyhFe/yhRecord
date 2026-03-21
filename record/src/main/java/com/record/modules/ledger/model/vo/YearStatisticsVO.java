package com.record.modules.ledger.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 年度统计返回值。
 * 按标签聚合金额，并计算每个标签的占比。
 */
@Data
@Builder
@Schema(description = "年度统计返回值")
public class YearStatisticsVO {
    @Schema(description = "统计年份", example = "2026")
    private int year;

    @Schema(description = "按标签汇总的金额列表")
    private List<TagAmountVO> items;

    @Data
    @Builder
    @Schema(description = "单个标签的统计结果")
    public static class TagAmountVO {
        @Schema(description = "标签 ID", example = "1")
        private Long tagId;

        @Schema(description = "标签名称", example = "饮食")
        private String tagName;

        @Schema(description = "累计金额", example = "356.50")
        private BigDecimal amount;

        @Schema(description = "金额占比", example = "0.35")
        private BigDecimal ratio;
    }
}
