package com.record.modules.diary.vo;

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
 * 列表页和详情页都复用这套结构。
 */
@Data
@Builder
@Schema(description = "日记返回对象")
public class DiaryVO {
    /** 日记 ID。 */
    @Schema(description = "日记 ID", example = "1")
    private Long id;

    /** 标题。 */
    @Schema(description = "日记标题", example = "今天的晚霞很好看")
    private String title;

    /** 正文。 */
    @Schema(description = "日记正文")
    private String content;

    /** 记录日期。 */
    @Schema(description = "记录日期", example = "2026-03-21")
    private LocalDate recordDate;

    /** 天气。 */
    @Schema(description = "天气", example = "晴")
    private String weather;

    /** 心情。 */
    @Schema(description = "心情", example = "开心")
    private String mood;

    /** 可见范围。 */
    @Schema(description = "可见范围", example = "PRIVATE")
    private VisibilityType visibility;

    /** 日记提醒时间。 */
    @Schema(description = "提醒时间", example = "2026-03-21 21:00:00")
    private LocalDateTime remindAt;

    /** 地点名称。 */
    @Schema(description = "地点名称", example = "深圳湾公园")
    private String locationName;

    /** 完整地址。 */
    @Schema(description = "完整地址", example = "广东省深圳市南山区深圳湾公园")
    private String address;

    /** 省份。 */
    @Schema(description = "省份", example = "广东省")
    private String province;

    /** 城市。 */
    @Schema(description = "城市", example = "深圳市")
    private String city;

    /** 区县。 */
    @Schema(description = "区县", example = "南山区")
    private String district;

    /** 纬度。 */
    @Schema(description = "纬度", example = "22.5039")
    private Double latitude;

    /** 经度。 */
    @Schema(description = "经度", example = "113.9506")
    private Double longitude;

    /** 定位来源。 */
    @Schema(description = "定位来源", example = "MANUAL")
    private LocationSourceType locationSourceType;

    /** 点赞数。 */
    @Schema(description = "点赞数", example = "3")
    private Integer likeCount;

    /** 评论数。 */
    @Schema(description = "评论数", example = "2")
    private Integer commentCount;

    /** 年龄文案。 */
    @Schema(description = "年龄差值文案", example = "记于22岁1月10天")
    private String ageLabel;

    /** 附件路径列表。 */
    @Schema(description = "附件路径列表")
    private List<String> mediaPaths;

    /** 关联标签 ID 列表。 */
    @Schema(description = "关联标签 ID 列表")
    private List<Long> tagIds;
}
