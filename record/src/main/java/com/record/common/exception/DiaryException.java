package com.record.common.exception;

public class DiaryException extends BusinessException {

    public DiaryException(String message) {
        super(ErrorCode.DIARY_ERROR, message);
    }
}
