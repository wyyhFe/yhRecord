package com.record.modules.ledger.vo;

import com.record.common.enums.LedgerType;
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
public class LedgerEntryVO {
    /** 流水 ID。 */
    private Long id;
    /** 所属账本 ID。 */
    private Long bookId;
    /** 收支类型。 */
    private LedgerType type;
    /** 金额。 */
    private BigDecimal amount;
    /** 记账日期。 */
    private LocalDate entryDate;
    /** 备注。 */
    private String remark;
    /** 图片路径。 */
    private String imagePath;
    /** 关联标签 ID 列表。 */
    private List<Long> tagIds;
}
