package com.record.modules.location.controller;

import com.record.common.model.ApiResponse;
import com.record.modules.location.dto.ReverseGeocodeRequest;
import com.record.modules.location.dto.ValidatePathRequest;
import com.record.modules.location.service.LocationService;
import com.record.modules.location.vo.LocationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 定位辅助接口。
 * 提供逆地理编码和 OSS 路径校验两类能力。
 */
@Tag(name = "定位")
@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 根据经纬度解析地址。
     */
    @Operation(summary = "逆地理编码")
    @PostMapping("/reverse-geocode")
    public ApiResponse<LocationVO> reverseGeocode(@Valid @RequestBody ReverseGeocodeRequest request) {
        return ApiResponse.success(locationService.reverseGeocode(request.getLatitude(), request.getLongitude()));
    }

    /**
     * 校验文件路径是否合法。
     */
    @Operation(summary = "校验文件路径")
    @PostMapping("/validate")
    public ApiResponse<Map<String, Boolean>> validate(@Valid @RequestBody ValidatePathRequest request) {
        return ApiResponse.success(Map.of("valid", locationService.validateFilePath(request.getFilePath())));
    }
}
