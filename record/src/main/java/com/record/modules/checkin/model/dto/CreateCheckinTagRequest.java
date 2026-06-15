package com.record.modules.checkin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建自定义打卡标签请求。
 */
@Data
@Schema(description = "创建自定义打卡标签请求")
public class CreateCheckinTagRequest {

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "游泳")
    @NotBlank
    @Size(max = 32)
    private String name;

    @Schema(description = "标签图标 Emoji", example = "🏊")
    @Size(max = 8)
    private String icon;
}
