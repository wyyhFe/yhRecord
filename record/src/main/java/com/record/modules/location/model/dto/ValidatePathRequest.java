package com.record.modules.location.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文件路径校验请求体。
 */
@Data
@Schema(description = "文件路径校验请求体")
public class ValidatePathRequest {

    @Schema(description = "需要校验的 OSS 相对路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "diary/20260321/demo.jpg")
    @NotBlank
    private String filePath;
}
