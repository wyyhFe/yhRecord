package com.record.modules.diary.model.dto;

import com.record.common.enums.VisibilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建日记请求体。
 */
@Data
@Schema(description = "创建日记请求体")
public class CreateDiaryRequest {

    @Schema(description = "日记标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "今天的晚霞很好看")
    @NotBlank
    private String title;

    @Schema(description = "日记正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String content;

    @Schema(description = "记录日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-21")
    @NotNull
    private LocalDate recordDate;

    @Schema(description = "天气", example = "晴")
    private String weather;

    @Schema(description = "心情", example = "开心")
    private String mood;

    @Schema(description = "日记可见范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRIVATE")
    @NotNull
    private VisibilityType visibility;

    @Schema(description = "定位信息")
    @Valid
    private DiaryLocationDTO location;

    @Schema(description = "附件列表")
    @Valid
    private List<DiaryMediaDTO> mediaList;

    @Schema(description = "关联标签 ID 列表")
    private List<Long> tagIds;
}
