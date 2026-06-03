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
@Schema(description = "RAG 检索结果")
public class RagSearchResultVO {

    @Schema(description = "命中的切片列表")
    private List<ChunkHitVO> hits;

    @Schema(description = "总命中数")
    private int totalHits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单条切片命中结果")
    public static class ChunkHitVO {

        @Schema(description = "切片 ID")
        private Long chunkId;

        @Schema(description = "文档 ID")
        private Long documentId;

        @Schema(description = "文档标题")
        private String documentTitle;

        @Schema(description = "切片序号")
        private Integer chunkIndex;

        @Schema(description = "切片内容")
        private String content;

        @Schema(description = "相关性分数（MySQL 全文检索相关度）")
        private Double relevanceScore;
    }
}
