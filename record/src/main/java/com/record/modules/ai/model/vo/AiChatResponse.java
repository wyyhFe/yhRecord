package com.record.modules.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "AI 聊天响应")
public class AiChatResponse {

    @Schema(description = "会话 ID", example = "ledger-home")
    private String conversationId;

    @Schema(description = "AI 回复")
    private String reply;
}
