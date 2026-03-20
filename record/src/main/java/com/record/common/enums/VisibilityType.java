package com.record.common.enums;

/**
 * 日记可见范围。
 * PRIVATE 仅自己可见，SHARED 用于共享日记本，PUBLIC 为公开内容。
 */
public enum VisibilityType {
    /** 仅作者自己可见。 */
    PRIVATE,
    /** 共享日记场景下对成员可见。 */
    SHARED,
    /** 公开可见。 */
    PUBLIC
}
