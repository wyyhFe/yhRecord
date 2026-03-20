package com.record.modules.diary.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.LocationSourceType;
import com.record.common.enums.VisibilityType;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日记实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diary")
public class Diary extends BaseEntity {
    @TableId
    private Long id;
    /** 作者用户 ID。 */
    private Long userId;
    /** 标题。 */
    private String title;
    /** 正文内容。 */
    private String content;
    /** 记录日期。 */
    private LocalDate recordDate;
    /** 天气。 */
    private String weather;
    /** 心情。 */
    private String mood;
    /** 可见范围。 */
    private VisibilityType visibility;
    /** 单篇日记提醒时间。 */
    private LocalDateTime remindAt;
    /** 地点名称。 */
    private String locationName;
    /** 完整地址。 */
    private String address;
    /** 省份。 */
    private String province;
    /** 城市。 */
    private String city;
    /** 区县。 */
    private String district;
    /** 纬度。 */
    private Double latitude;
    /** 经度。 */
    private Double longitude;
    /** 定位来源。 */
    private LocationSourceType locationSourceType;
    /** 点赞数缓存。 */
    private Integer likeCount;
    /** 评论数缓存。 */
    private Integer commentCount;
    /** 软删除时间。 */
    private LocalDateTime deletedAt;
}
