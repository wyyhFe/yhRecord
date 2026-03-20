package com.record.modules.location.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 逆地理编码请求体。
 * 前端只传经纬度，后端通过地图服务补全结构化地址。
 */
@Data
public class ReverseGeocodeRequest {
    /** 纬度。 */
    @NotNull
    private Double latitude;
    /** 经度。 */
    @NotNull
    private Double longitude;
}
