package com.record.modules.checkin.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录实体。
 * 同一任务同一用户同一天只允许有一条有效记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin_record")
public class CheckinRecord extends BaseEntity {
    @TableId
    private Long id;
    /** 所属任务 ID。 */
    private Long taskId;
    /** 打卡用户 ID。 */
    private Long userId;
    /** 业务打卡日期。 */
    private LocalDate checkinDate;
    /** 实际点击打卡的时间。 */
    private LocalDateTime checkedAt;
    /** 打卡备注。 */
    private String remark;
}
