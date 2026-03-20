package com.record.modules.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 日记附件结构。
 */
@Data
public class DiaryMediaDTO {
    /** 附件类型，如 IMAGE / VIDEO。 */
    @NotBlank
    private String mediaType;
    /** OSS 相对路径。 */
    @NotBlank
    private String filePath;
    /** 附件排序值。 */
    private Integer sortOrder;
}
