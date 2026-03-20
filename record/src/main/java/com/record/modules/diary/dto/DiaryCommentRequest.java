package com.record.modules.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 日记评论请求体。
 */
@Data
public class DiaryCommentRequest {
    /** 评论内容。 */
    @NotBlank
    private String content;
}
