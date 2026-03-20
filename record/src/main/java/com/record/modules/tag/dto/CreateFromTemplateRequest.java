package com.record.modules.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 基于模板创建标签请求体。
 */
@Data
@Schema(description = "基于模板创建标签请求体")
public class CreateFromTemplateRequest {
    @Schema(description = "模板 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    private Long templateId;

    @Schema(description = "覆盖后的标签名称", example = "旅行")
    private String name;

    @Schema(description = "覆盖后的标签颜色", example = "#FF8A65")
    private String color;

    @Schema(description = "覆盖后的标签图标", example = "plane")
    private String icon;
}
