package com.record.common.enums;

/**
 * 回收站资源类型。
 * 用于标记被删除的数据原本属于哪个业务模块。
 */
public enum ResourceType {
    /** 日记。 */
    DIARY,
    /** 记账流水。 */
    LEDGER_ENTRY,
    /** 打卡任务。 */
    CHECKIN_TASK,
    /** 纪念日。 */
    MEMORIAL_DAY
}
