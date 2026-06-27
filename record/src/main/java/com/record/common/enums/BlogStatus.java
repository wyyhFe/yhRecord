package com.record.common.enums;

/**
 * 博客文章状态。
 */
public enum BlogStatus {
    /** 草稿，仅作者可见。 */
    DRAFT,
    /** 已发布，公开可见。 */
    PUBLISHED,
    /** 已归档，仅列表可见但不出现在时间线上。 */
    ARCHIVED
}
