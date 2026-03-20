package com.record.modules.location.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 结构化定位返回对象。
 */
@Data
@Builder
public class LocationVO {
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
}
