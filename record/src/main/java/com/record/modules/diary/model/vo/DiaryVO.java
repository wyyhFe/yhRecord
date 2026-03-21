package com.record.modules.diary.model.vo;

import com.record.common.enums.LocationSourceType;
import com.record.common.enums.VisibilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日记返回对象。
 * 列表页和详情页复用这一套结构。
 */
@Data
@Builder
@Schema(description = "日记返回对象")
public class DiaryVO {

    @Schema(description = "日记 ID", example = "1")
    private Long id;

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

    @Schema(description = "提醒时间", example = "2026-03-21 21:00:00")
    private LocalDateTime remindAt;

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

    @Schema(description = "年龄文案", example = "记于22岁1月10天")
    private String ageLabel;

    @Schema(description = "附件路径列表")
    private List<String> mediaPaths;

    @Schema(description = "标签 ID 列表")
    private List<Long> tagIds;
}
