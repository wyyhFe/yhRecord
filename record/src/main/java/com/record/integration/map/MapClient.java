package com.record.integration.map;

import com.record.common.config.AppProperties;
import com.record.common.exception.LocationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 地图服务客户端。
 */
@Component
public class MapClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public MapClient(RestTemplate restTemplate, AppProperties appProperties) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
    }

    public ReverseGeocodeResponse reverseGeocode(Double latitude, Double longitude) {
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getMap().getReverseGeocodeUrl())
                .queryParam("key", appProperties.getMap().getApiKey())
                .queryParam("location", latitude + "," + longitude)
                .toUriString();
        ReverseGeocodeResponse response = restTemplate.getForObject(url, ReverseGeocodeResponse.class);
        if (response == null || response.getStatus() == null || response.getStatus() != 0) {
            throw new LocationException("逆地理编码失败");
        }
        return response;
    }
}
