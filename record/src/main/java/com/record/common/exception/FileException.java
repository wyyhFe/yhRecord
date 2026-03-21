package com.record.common.exception;

public class FileException extends BusinessException {

    public FileException(String message) {
        super(ErrorCode.FILE_ERROR, message);
    }
}

