package com.record.modules.diary.model.dto;

import com.record.common.enums.VisibilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建日记请求体。
 */
@Data
@Schema(description = "创建日记请求体")
public class CreateDiaryRequest {
    /** 日记标题。 */
    @Schema(description = "日记标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "今天的晚霞很好看")
    @NotBlank
    private String title;

    /** 日记正文。 */
    @Schema(description = "日记正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String content;

    /** 记录日期。 */
    @Schema(description = "记录日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-21")
    @NotNull
    private LocalDate recordDate;

    /** 天气。 */
    @Schema(description = "天气", example = "晴")
    private String weather;

    /** 心情。 */
    @Schema(description = "心情", example = "开心")
    private String mood;

    /** 日记可见范围。 */
    @Schema(description = "日记可见范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRIVATE")
    @NotNull
    private VisibilityType visibility;

    /** 日记提醒时间，可选。 */
    @Schema(description = "提醒时间", example = "2026-03-21 21:00:00")
    private LocalDateTime remindAt;

    /** 定位信息。 */
    @Schema(description = "定位信息")
    @Valid
    private DiaryLocationDTO location;

    /** 附件列表。 */
    @Schema(description = "附件列表")
    @Valid
    private List<DiaryMediaDTO> mediaList;

    /** 关联标签 ID 列表。 */
    @Schema(description = "关联标签 ID 列表")
    private List<Long> tagIds;
}
