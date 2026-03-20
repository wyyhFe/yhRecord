package com.record.modules.tag.dto;

import com.record.common.enums.TagModuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建自定义标签请求体。
 */
@Data
@Schema(description = "创建自定义标签请求体")
public class CreateTagRequest {
    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "旅行")
    @NotBlank
    private String name;

    @Schema(description = "标签颜色", example = "#FF8A65")
    private String color;

    @Schema(description = "标签图标", example = "plane")
    private String icon;

    @Schema(description = "所属模块", requiredMode = Schema.RequiredMode.REQUIRED, example = "DIARY")
    @NotNull
    private TagModuleType moduleType;
}
