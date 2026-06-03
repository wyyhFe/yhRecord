package com.record.modules.knowledge.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "RAG 问答响应")
public class RagAnswerVO {

    @Schema(description = "AI 生成的回答内容")
    private String answer;

    @Schema(description = "检索到的参考切片")
    private List<RagSearchResultVO.ChunkHitVO> references;

    @Schema(description = "参考切片数")
    private int referenceCount;
}
