package com.record.modules.knowledge.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建知识库请求")
public class CreateKnowledgeBaseRequest {

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "记账帮助中心")
    @NotBlank
    private String name;

    @Schema(description = "知识库唯一编码，不传则自动生成", example = "ledger-help")
    private String code;

    @Schema(description = "知识库描述", example = "用于存放账单分析和产品说明文档")
    private String description;

    @Schema(description = "可见范围，PRIVATE / PUBLIC", example = "PRIVATE")
    private String visibility;
}
