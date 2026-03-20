package com.record.common.exception;

public class AuthException extends BusinessException {

    public AuthException(String message) {
        super(ErrorCode.AUTH_ERROR, message);
    }
}
