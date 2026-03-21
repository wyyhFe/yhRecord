package com.record.modules.file.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取 OSS 上传签名请求体。
 */
@Data
@Schema(description = "获取 OSS 上传签名请求体")
public class UploadPolicyRequest {
    @Schema(description = "上传目录前缀", requiredMode = Schema.RequiredMode.REQUIRED, example = "diary/20260321")
    @NotBlank
    private String dir;
    @Schema(description = "签名有效秒数", example = "300")
    private int expireSeconds = 300;
}
