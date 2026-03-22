package com.record.modules.tag.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.enums.LedgerTagType;
import com.record.common.enums.TagModuleType;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签模板实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag_template")
@Schema(description = "标签模板实体")
public class TagTemplate extends BaseEntity {

    @TableId
    @Schema(description = "模板 ID", example = "1")
    private Long id;

    @Schema(description = "模板名称", example = "旅行")
    private String name;

    @Schema(description = "模板颜色", example = "#FF8A65")
    private String color;

    @Schema(description = "模板图标", example = "plane")
    private String icon;

    @Schema(description = "所属模块", example = "DIARY")
    private TagModuleType moduleType;

    @Schema(description = "记账标签类型，仅 LEDGER 模块使用", example = "EXPENSE")
    private LedgerTagType ledgerType;

    @Schema(description = "排序值", example = "10")
    private Integer sortOrder;

    @Schema(description = "状态", example = "ENABLED")
    private CommonStatus status;
}
