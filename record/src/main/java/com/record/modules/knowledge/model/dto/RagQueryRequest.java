package com.record.modules.knowledge.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "知识库 RAG 问答请求")
public class RagQueryRequest {

    @Schema(description = "知识库 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    private Long knowledgeBaseId;

    @Schema(description = "用户问题", requiredMode = Schema.RequiredMode.REQUIRED, example = "帮我总结一下上个月的开销情况")
    @NotBlank
    private String question;

    @Schema(description = "检索返回的最相关切片数量（默认 5，上限 20）", example = "5")
    private Integer topK;
}
