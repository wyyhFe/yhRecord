package com.record.modules.diary.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 日记附件结构。
 */
@Data
@Schema(description = "日记附件结构")
public class DiaryMediaDTO {

    @Schema(description = "附件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "IMAGE")
    @NotBlank
    private String mediaType;

    @Schema(description = "OSS 相对路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "diary/20260321/demo.jpg")
    @NotBlank
    private String filePath;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
}
