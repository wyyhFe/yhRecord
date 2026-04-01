package com.record.modules.knowledge.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KnowledgeDocumentVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeBaseId;

    private String title;

    private String sourceType;

    private String fileName;

    private String filePath;

    private String mimeType;

    private String contentHash;

    private String status;

    private Integer chunkCount;

    private String lastError;

    private LocalDateTime createdAt;
}
