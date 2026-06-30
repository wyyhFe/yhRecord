package com.record.modules.diary.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.LocationSourceType;
import com.record.common.enums.VisibilityType;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日记实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_diary")
@Schema(description = "日记实体")
public class Diary extends BaseEntity {

    @TableId
    @Schema(description = "日记 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "标题", example = "今天去了海边")
    private String title;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "记录日期", example = "2026-03-21")
    private LocalDate recordDate;

    @Schema(description = "天气", example = "晴")
    private String weather;

    @Schema(description = "心情", example = "开心")
    private String mood;

    @Schema(description = "可见范围", example = "PRIVATE")
    private VisibilityType visibility;

    @Schema(description = "地点名称", example = "深圳湾公园")
    private String locationName;

    @Schema(description = "完整地址", example = "广东省深圳市南山区深圳湾公园")
    private String address;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区县", example = "南山区")
    private String district;

    @Schema(description = "纬度", example = "22.5039")
    private Double latitude;

    @Schema(description = "经度", example = "113.9506")
    private Double longitude;

    @Schema(description = "定位来源", example = "MANUAL")
    private LocationSourceType locationSourceType;

    @Schema(description = "点赞数", example = "3")
    private Integer likeCount;

    @Schema(description = "评论数", example = "2")
    private Integer commentCount;

    @Schema(description = "浏览次数", example = "42")
    private Integer viewCount;

    @Schema(description = "软删除时间", example = "2026-03-21T22:00:00")
    private LocalDateTime deletedAt;
}
