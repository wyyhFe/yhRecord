package com.record.modules.diary.dto;

import com.record.common.enums.VisibilityType;
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
public class CreateDiaryRequest {
    /** 日记标题。 */
    @NotBlank
    private String title;
    /** 日记正文。 */
    @NotBlank
    private String content;
    /** 记录日期。 */
    @NotNull
    private LocalDate recordDate;
    /** 天气。 */
    private String weather;
    /** 心情。 */
    private String mood;
    /** 日记可见范围。 */
    @NotNull
    private VisibilityType visibility;
    /** 日记提醒时间，可选。 */
    private LocalDateTime remindAt;
    /** 定位信息。 */
    @Valid
    private DiaryLocationDTO location;
    /** 附件列表。 */
    @Valid
    private List<DiaryMediaDTO> mediaList;
    /** 关联标签 ID 列表。 */
    private List<Long> tagIds;
}
