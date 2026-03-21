package com.record.modules.tag.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 从模板创建标签请求体。
 */
@Data
@Schema(description = "从模板创建标签请求体")
public class CreateFromTemplateRequest {

    @Schema(description = "模板 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    private Long templateId;

    @Schema(description = "自定义名称", example = "旅行")
    private String name;

    @Schema(description = "自定义颜色", example = "#FF8A65")
    private String color;

    @Schema(description = "自定义图标", example = "plane")
    private String icon;
}
