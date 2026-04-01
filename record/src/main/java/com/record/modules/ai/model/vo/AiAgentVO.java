package com.record.modules.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "AI Agent 摘要")
public class AiAgentVO {

    @Schema(description = "Agent ID")
    private String id;

    @Schema(description = "Agent 名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "能力标签")
    private List<String> capabilities;
}
