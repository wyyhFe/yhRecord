package com.record.common.enums;

/**
 * 日记定位来源。
 * CURRENT 表示当前位置，MANUAL 表示用户手动选点。
 */
public enum LocationSourceType {
    /** 通过当前定位能力获取。 */
    CURRENT,
    /** 用户在地图上手动选择。 */
    MANUAL
}
