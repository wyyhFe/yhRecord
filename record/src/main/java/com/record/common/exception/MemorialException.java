package com.record.common.exception;

public class MemorialException extends BusinessException {

    public MemorialException(String message) {
        super(ErrorCode.MEMORIAL_ERROR, message);
    }
}
