package com.record.modules.location.service.impl;

import com.record.common.config.AppProperties;
import com.record.common.exception.FileException;
import com.record.integration.map.MapClient;
import com.record.integration.map.ReverseGeocodeResponse;
import com.record.modules.location.model.vo.LocationVO;
import com.record.modules.location.service.LocationService;
import org.springframework.stereotype.Service;

/**
 * 定位服务实现。
 */
@Service
public class LocationServiceImpl implements LocationService {

    private final MapClient mapClient;
    private final AppProperties appProperties;

    public LocationServiceImpl(MapClient mapClient, AppProperties appProperties) {
        this.mapClient = mapClient;
        this.appProperties = appProperties;
    }

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

    @Override
    public boolean validateFilePath(String filePath) {
        boolean matched = appProperties.getFile().getAllowedPathPrefixes().stream().anyMatch(filePath::startsWith);
        if (!matched) {
            throw new FileException("文件路径不在允许范围内");
        }
        return true;
    }
}
