package com.record.modules.ledger.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类金额（饼图用）。
 */
@Data
@Builder
@Schema(description = "分类金额")
public class CategoryAmountVO {

    @Schema(description = "标签 ID", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tagId;

    @Schema(description = "标签名称", example = "餐饮")
    private String tagName;

    @Schema(description = "累计金额", example = "356.50")
    private BigDecimal amount;

    @Schema(description = "金额占比", example = "0.35")
    private BigDecimal ratio;
}
