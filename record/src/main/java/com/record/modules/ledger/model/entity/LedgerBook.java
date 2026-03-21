package com.record.modules.ledger.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账本实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ledger_book")
@Schema(description = "账本实体")
public class LedgerBook extends BaseEntity {

    @TableId
    @Schema(description = "账本 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "账本名称", example = "生活开销")
    private String name;

    @Schema(description = "账本描述", example = "用于记录日常生活支出")
    private String description;
}
