package com.record.modules.checkin.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 打卡任务实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin_task")
public class CheckinTask extends BaseEntity {
    @TableId
    private Long id;
    /** 任务创建人 ID。 */
    private Long userId;
    /** 任务名称。 */
    private String name;
    /** 任务描述。 */
    private String description;
    /** 任务开始日期。 */
    private LocalDate startDate;
    /** 任务状态。 */
    private CommonStatus status;
}
