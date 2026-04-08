package com.record.modules.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "AI function calling demo response")
public class AiFunctionCallResponse {

    @Schema(description = "AI reply")
    private String reply;

    @Schema(description = "Tool name")
    private String toolName;

    @Schema(description = "Tool result")
    private Object toolResult;
}
