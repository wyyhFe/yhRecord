package com.record.modules.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Agent 聊天请求")
public class AgentChatRequest {

    @NotBlank(message = "消息不能为空")
    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "会话 ID")
    private String conversationId;

    @Schema(description = "知识库 ID，可选")
    private Long knowledgeBaseId;
}
