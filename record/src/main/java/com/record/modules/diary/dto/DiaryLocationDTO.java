package com.record.modules.diary.dto;

import com.record.common.enums.LocationSourceType;
import lombok.Data;

/**
 * 日记定位结构。
 * 前端传当前位置或手动选点的信息时使用。
 */
@Data
public class DiaryLocationDTO {
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
    private LocationSourceType sourceType;
}
