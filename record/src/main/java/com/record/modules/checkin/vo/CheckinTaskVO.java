package com.record.modules.checkin.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡任务返回对象。
 * 用于任务列表、日历详情等前端展示场景。
 */
@Data
@Builder
public class CheckinTaskVO {
    /** 任务 ID。 */
    private Long id;
    /** 任务名称。 */
    private String name;
    /** 任务描述。 */
    private String description;
    /** 任务开始日期。 */
    private LocalDate startDate;
    /** 累计打卡次数。 */
    private long totalCount;
    /** 最近一次打卡时间。 */
    private LocalDateTime latestCheckedAt;
}
