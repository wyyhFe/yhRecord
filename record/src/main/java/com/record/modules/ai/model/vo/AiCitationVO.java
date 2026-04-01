package com.record.modules.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "AI 引用来源")
public class AiCitationVO {

    @Schema(description = "引用 ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "引用片段")
    private String snippet;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源路径")
    private String sourcePath;

    @Schema(description = "匹配分数")
    private Double score;
}
