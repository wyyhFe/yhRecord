package com.record.common.exception;

public class CheckinException extends BusinessException {

    public CheckinException(String message) {
        super(ErrorCode.CHECKIN_ERROR, message);
    }
}

