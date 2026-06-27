package com.record.common.exception;

public class BlogException extends BusinessException {

    public BlogException(String message) {
        super(ErrorCode.BLOG_ERROR, message);
    }
}
