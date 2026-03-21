package com.record.modules.ledger.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.LedgerType;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 记账流水实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ledger_entry")
@Schema(description = "记账流水实体")
public class LedgerEntry extends BaseEntity {

    @TableId
    @Schema(description = "流水主键 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "流水类型", example = "EXPENSE")
    private LedgerType type;

    @Schema(description = "金额，必须大于 0", example = "52.30")
    private BigDecimal amount;

    @Schema(description = "记账日期", example = "2026-03-21")
    private LocalDate entryDate;

    @Schema(description = "备注", example = "午餐")
    private String remark;

    @Schema(description = "图片 OSS 路径", example = "ledger/20260321/demo.jpg")
    private String imagePath;
}
