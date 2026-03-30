package com.record.modules.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "账单分析历史")
public class BillAnalysisHistoryVO {

    @Schema(description = "分析记录 ID")
    private String id;

    @Schema(description = "分析时间")
    private LocalDateTime createdAt;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "账本 ID")
    private Long bookId;

    @Schema(description = "账本名称")
    private String bookName;

    @Schema(description = "账单条数")
    private int entryCount;

    @Schema(description = "分析摘要")
    private String summary;

    @Schema(description = "补充问题")
    private String question;
}
