package com.record.modules.location.service;

import com.record.modules.location.vo.LocationVO;

public interface LocationService {
    LocationVO reverseGeocode(Double latitude, Double longitude);
    boolean validateFilePath(String filePath);
}
