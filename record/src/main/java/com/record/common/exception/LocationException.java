package com.record.common.exception;

public class LocationException extends BusinessException {

    public LocationException(String message) {
        super(ErrorCode.LOCATION_ERROR, message);
    }
}
