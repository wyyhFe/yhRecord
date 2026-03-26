package com.record.modules.ledger.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 账本返回对象。
 */
@Data
@Builder
@Schema(description = "账本返回对象")
public class LedgerBookVO {

    @Schema(description = "账本 ID", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "账本名称", example = "生活开销")
    private String name;

    @Schema(description = "账本描述", example = "用于记录日常生活支出")
    private String description;
}
