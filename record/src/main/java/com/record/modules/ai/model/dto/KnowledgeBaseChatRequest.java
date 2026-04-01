package com.record.modules.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "知识库聊天请求")
public class KnowledgeBaseChatRequest {

    @NotNull(message = "知识库 ID 不能为空")
    @Schema(description = "知识库 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long knowledgeBaseId;

    @NotBlank(message = "消息不能为空")
    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "会话 ID")
    private String conversationId;
}
