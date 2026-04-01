package com.record.modules.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建 AI 会话请求")
public class CreateConversationRequest {

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "Agent ID")
    private String agentId;

    @Schema(description = "知识库 ID")
    private Long knowledgeBaseId;
}
