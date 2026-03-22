package com.record.modules.tag.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.LedgerTagType;
import com.record.common.enums.TagModuleType;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户自定义标签实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_tag")
@Schema(description = "用户自定义标签实体")
public class UserTag extends BaseEntity {

    @TableId
    @Schema(description = "标签 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "来源模板 ID，可为空", example = "1")
    private Long templateId;

    @Schema(description = "标签名称", example = "旅行")
    private String name;

    @Schema(description = "标签颜色", example = "#FF8A65")
    private String color;

    @Schema(description = "标签图标", example = "plane")
    private String icon;

    @Schema(description = "所属模块", example = "DIARY")
    private TagModuleType moduleType;

    @Schema(description = "记账标签类型，仅 LEDGER 模块使用", example = "EXPENSE")
    private LedgerTagType ledgerType;
}
