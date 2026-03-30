package com.record.modules.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(description = "账单分析响应")
public class BillAnalysisResponse {

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "账本 ID")
    private Long bookId;

    @Schema(description = "账本名称")
    private String bookName;

    @Schema(description = "账单条数")
    private int entryCount;

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "总支出")
    private BigDecimal totalExpense;

    @Schema(description = "结余，收入减支出")
    private BigDecimal balance;

    @Schema(description = "支出分类汇总")
    private List<CategoryAmountVO> expenseCategories;

    @Schema(description = "收入分类汇总")
    private List<CategoryAmountVO> incomeCategories;

    @Schema(description = "高频账单样本")
    private List<EntrySampleVO> samples;

    @Schema(description = "AI 生成的总结")
    private String summary;

    @Schema(description = "AI 观察结论")
    private List<String> observations;

    @Schema(description = "AI 风险提示")
    private List<String> risks;

    @Schema(description = "AI 建议")
    private List<String> suggestions;

    @Data
    @Builder
    @Schema(description = "分类金额")
    public static class CategoryAmountVO {
        @Schema(description = "分类名称", example = "餐饮")
        private String name;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "占比")
        private BigDecimal ratio;
    }

    @Data
    @Builder
    @Schema(description = "账单样本")
    public static class EntrySampleVO {
        @Schema(description = "日期")
        private LocalDate entryDate;

        @Schema(description = "收支类型")
        private String type;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "账本名称")
        private String bookName;

        @Schema(description = "分类名称")
        private String category;

        @Schema(description = "备注")
        private String remark;
    }
}
