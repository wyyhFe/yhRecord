package com.record.modules.checkin.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 执行打卡请求体。
 */
@Data
public class CheckinRequest {
    /** 打卡归属日期，默认可传今天。 */
    private LocalDate checkinDate;
    /** 打卡备注。 */
    private String remark;
}
