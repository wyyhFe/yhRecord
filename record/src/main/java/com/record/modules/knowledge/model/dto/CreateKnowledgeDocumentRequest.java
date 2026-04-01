package com.record.modules.knowledge.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建知识库文档请求")
public class CreateKnowledgeDocumentRequest {

    @Schema(description = "知识库 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    private Long knowledgeBaseId;

    @Schema(description = "文档标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "账单分析指引")
    @NotBlank
    private String title;

    @Schema(description = "来源类型，UPLOAD / URL / MANUAL", requiredMode = Schema.RequiredMode.REQUIRED, example = "UPLOAD")
    @NotBlank
    private String sourceType;

    @Schema(description = "原始文件名", example = "bill-guide.pdf")
    private String fileName;

    @Schema(description = "原始文件路径或对象存储地址", example = "oss://record/kb/bill-guide.pdf")
    private String filePath;

    @Schema(description = "文件类型", example = "application/pdf")
    private String mimeType;

    @Schema(description = "内容哈希", example = "a1b2c3d4")
    private String contentHash;
}
