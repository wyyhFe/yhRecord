package com.record.modules.ledger.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 账本返回对象。
 */
@Data
@Builder
public class LedgerBookVO {
    /** 账本 ID。 */
    private Long id;
    /** 账本名称。 */
    private String name;
    /** 账本描述。 */
    private String description;
}
