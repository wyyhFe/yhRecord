package com.record.modules.diary.model.dto;

import com.record.common.enums.LocationSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日记定位结构。
 */
@Data
@Schema(description = "日记定位结构")
public class DiaryLocationDTO {
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
    private LocationSourceType sourceType;
}
