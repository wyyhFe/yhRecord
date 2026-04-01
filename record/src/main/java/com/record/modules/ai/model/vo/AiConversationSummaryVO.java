package com.record.modules.ai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiConversationSummaryVO {

    private String id;

    private String title;

    private String agentId;

    private String knowledgeBaseId;

    private String lastMessagePreview;

    private LocalDateTime updatedAt;

    private Integer messageCount;
}
