package com.record.modules.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "AI 聊天请求")
public class AiChatRequest {

    @NotBlank(message = "消息不能为空")
    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "会话 ID", example = "ledger-home")
    private String conversationId;
}
