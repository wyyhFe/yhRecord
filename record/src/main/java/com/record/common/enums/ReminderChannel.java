package com.record.common.enums;

/**
 * 提醒消息发送通道。
 * 当前主通道为小程序订阅消息，公众号模板消息作为扩展通道保留。
 */
public enum ReminderChannel {
    /** 小程序订阅消息。 */
    MINI_PROGRAM,
    /** 公众号模板消息。 */
    OFFICIAL_ACCOUNT
}
