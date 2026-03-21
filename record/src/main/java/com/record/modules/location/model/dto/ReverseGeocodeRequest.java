package com.record.modules.location.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 逆地理编码请求。
 * 前端传经纬度，后端再去地图服务换取结构化地址。
 */
@Data
@Schema(description = "逆地理编码请求")
public class ReverseGeocodeRequest {

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "22.5039")
    @NotNull
    private Double latitude;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "113.9506")
    @NotNull
    private Double longitude;
}
