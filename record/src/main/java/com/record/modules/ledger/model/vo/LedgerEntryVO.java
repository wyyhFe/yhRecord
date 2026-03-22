package com.record.modules.ledger.model.vo;

import com.record.common.enums.LedgerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 记账流水返回对象。
 */
@Data
@Builder
@Schema(description = "记账流水返回对象")
public class LedgerEntryVO {

    @Schema(description = "流水 ID", example = "1")
    private Long id;

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "收支类型", example = "EXPENSE")
    private LedgerType type;

    @Schema(description = "金额", example = "52.30")
    private BigDecimal amount;

    @Schema(description = "记账日期", example = "2026-03-21")
    private LocalDate entryDate;

    @Schema(description = "备注", example = "午餐")
    private String remark;

    @Schema(description = "图片路径", example = "ledger/20260321/demo.jpg")
    private String imagePath;

    @Schema(description = "关联标签 ID 列表")
    private List<Long> tagIds;

    @Schema(description = "关联标签明细")
    private List<TagItemVO> tags;

    @Data
    @Builder
    @Schema(description = "记账标签")
    public static class TagItemVO {
        @Schema(description = "标签 ID", example = "1")
        private Long id;

        @Schema(description = "标签名称", example = "餐饮")
        private String name;

        @Schema(description = "标签颜色", example = "#FF8A65")
        private String color;
    }
}
