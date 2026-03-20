package com.record.modules.tag.vo;

import com.record.common.enums.TagModuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 标签返回对象。
 */
@Data
@Builder
@Schema(description = "标签返回对象")
public class TagVO {
    @Schema(description = "标签 ID", example = "1")
    private Long id;

    @Schema(description = "来源模板 ID", example = "1")
    private Long templateId;

    @Schema(description = "标签名称", example = "旅行")
    private String name;

    @Schema(description = "标签颜色", example = "#FF8A65")
    private String color;

    @Schema(description = "标签图标", example = "plane")
    private String icon;

    @Schema(description = "所属模块", example = "DIARY")
    private TagModuleType moduleType;
}
