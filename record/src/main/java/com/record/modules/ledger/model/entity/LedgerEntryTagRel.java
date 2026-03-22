package com.record.modules.ledger.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流水与标签关联实体。
 */
@Data
@TableName("ledger_entry_tag_rel")
@Schema(description = "流水与标签关联实体")
public class LedgerEntryTagRel {

    @TableId
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "流水 ID", example = "101")
    private Long entryId;

    @Schema(description = "标签 ID", example = "12")
    private Long tagId;
}
