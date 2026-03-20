package com.record.modules.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新标签请求体。
 */
@Data
@Schema(description = "更新标签请求体")
public class UpdateTagRequest {
    @Schema(description = "标签名称", example = "旅行")
    private String name;

    @Schema(description = "标签颜色", example = "#FF8A65")
    private String color;

    @Schema(description = "标签图标", example = "plane")
    private String icon;
}
