package com.record.modules.location.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 结构化定位返回对象。
 */
@Data
@Builder
@Schema(description = "结构化定位返回对象")
public class LocationVO {

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
}
