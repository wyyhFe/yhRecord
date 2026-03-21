package com.record.modules.diary.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 日记评论请求体。
 */
@Data
@Schema(description = "日记评论请求体")
public class DiaryCommentRequest {

    @Schema(description = "父评论 ID，回复评论时传入", example = "10")
    private Long parentId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "今天也很开心")
    @NotBlank
    private String content;
}
