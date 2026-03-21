package com.record.common.exception;

public class TagException extends BusinessException {

    public TagException(String message) {
        super(ErrorCode.TAG_ERROR, message);
    }
}

