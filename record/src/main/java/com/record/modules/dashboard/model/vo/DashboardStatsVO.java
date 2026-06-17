package com.record.modules.dashboard.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "仪表盘统计数据")
public class DashboardStatsVO {

    @Schema(description = "用户总数")
    private Long userCount;

    @Schema(description = "日记总数")
    private Long diaryCount;

    @Schema(description = "打卡记录总数")
    private Long checkinCount;

    @Schema(description = "记账条目总数")
    private Long ledgerCount;

    @Schema(description = "纪念日总数")
    private Long memorialCount;

    @Schema(description = "知识库总数")
    private Long knowledgeCount;
}
