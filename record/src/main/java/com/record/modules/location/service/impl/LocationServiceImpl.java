package com.record.modules.location.service.impl;

import com.record.common.config.AppProperties;
import com.record.common.exception.FileException;
import com.record.integration.map.MapClient;
import com.record.integration.map.ReverseGeocodeResponse;
import com.record.modules.location.service.LocationService;
import com.record.modules.location.vo.LocationVO;
import org.springframework.stereotype.Service;

/**
 * 位置服务实现。
 * 一方面负责经纬度逆地理编码，另一方面负责 OSS 路径安全校验。
 */
@Service
public class LocationServiceImpl implements LocationService {

    private final MapClient mapClient;
    private final AppProperties appProperties;

    public LocationServiceImpl(MapClient mapClient, AppProperties appProperties) {
        this.mapClient = mapClient;
        this.appProperties = appProperties;
    }

    /**
     * 调用地图服务把经纬度转换成结构化地址。
     */
    @Override
    public LocationVO reverseGeocode(Double latitude, Double longitude) {
        ReverseGeocodeResponse response = mapClient.reverseGeocode(latitude, longitude);
        return LocationVO.builder()
                .latitude(latitude)
                .longitude(longitude)
                .address(response.getResult().getAddress())
                .province(response.getResult().getAddressComponent().getProvince())
                .city(response.getResult().getAddressComponent().getCity())
                .district(response.getResult().getAddressComponent().getDistrict())
                .build();
    }

    /**
     * 限制前端只提交允许目录下的 OSS 路径，避免越权引用任意文件。
     */
    @Override
    public boolean validateFilePath(String filePath) {
        boolean matched = appProperties.getFile().getAllowedPathPrefixes()
                .stream().anyMatch(filePath::startsWith);
        if (!matched) {
            throw new FileException("文件路径不合法");
        }
        return true;
    }
}
