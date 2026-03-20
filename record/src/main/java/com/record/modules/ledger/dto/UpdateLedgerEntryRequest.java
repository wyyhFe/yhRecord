package com.record.modules.ledger.dto;

import lombok.Data;

/**
 * 更新记账流水请求体。
 * 当前与创建字段一致，直接复用父类结构。
 */
@Data
public class UpdateLedgerEntryRequest extends CreateLedgerEntryRequest {
}
