package com.record.modules.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账本实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ledger_book")
public class LedgerBook extends BaseEntity {
    @TableId
    private Long id;
    /** 所属用户 ID。 */
    private Long userId;
    /** 账本名称。 */
    private String name;
    /** 账本描述。 */
    private String description;
}
