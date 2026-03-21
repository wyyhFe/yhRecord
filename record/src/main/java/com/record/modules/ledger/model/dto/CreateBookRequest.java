package com.record.modules.ledger.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建账本请求体。
 */
@Data
@Schema(description = "创建账本请求体")
public class CreateBookRequest {
    @Schema(description = "账本名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "生活开销")
    @NotBlank
    private String name;
    @Schema(description = "账本描述", example = "用于记录日常生活支出")
    private String description;
}
