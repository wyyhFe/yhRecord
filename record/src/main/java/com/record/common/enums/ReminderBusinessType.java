package com.record.common.enums;

/**
 * 提醒业务类型。
 * 用于区分这条提醒是“每日写日记提醒”还是“纪念日提醒”。
 */
public enum ReminderBusinessType {
    /** 当天未写日记时触发的每日提醒。 */
    DIARY_DAILY,
    /** 纪念日到期提醒。 */
    MEMORIAL_DAY
}
