package com.record.modules.ledger.entity;

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

    /** 所属用户 ID。 */
    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    /** 所属账本 ID。 */
    @Schema(description = "所属账本 ID", example = "1")
    private Long bookId;

    /** 流水类型。 */
    @Schema(description = "流水类型", example = "EXPENSE")
    private LedgerType type;

    /** 金额。 */
    @Schema(description = "金额", example = "52.30")
    private BigDecimal amount;

    /** 记账日期。 */
    @Schema(description = "记账日期", example = "2026-03-21")
    private LocalDate entryDate;

    /** 备注。 */
    @Schema(description = "备注", example = "午饭和咖啡")
    private String remark;

    /** 记账图片路径。 */
    @Schema(description = "记账图片路径", example = "ledger/20260321/demo.jpg")
    private String imagePath;
}
