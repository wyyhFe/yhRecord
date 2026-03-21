package com.record.modules.ledger.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新记账流水请求体。
 */
@Data
@Schema(description = "更新记账流水请求体")
public class UpdateLedgerEntryRequest extends CreateLedgerEntryRequest {
}
