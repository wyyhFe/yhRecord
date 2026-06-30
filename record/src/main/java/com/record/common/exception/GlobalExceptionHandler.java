package com.record.common.exception;

import com.record.common.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.failure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ApiResponse<Void> handleValidation(Exception ex) {
        return ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNotFound(NoResourceFoundException ex) {
        log.warn("接口不存在: {} {}", ex.getHttpMethod(), ex.getResourcePath());
        return ApiResponse.failure(ErrorCode.NOT_FOUND.getCode(), "接口不存在: " + ex.getResourcePath());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ApiResponse<Void> handleIllegalArgument(RuntimeException ex) {
        log.warn("参数状态异常: {}", ex.getMessage());
        return ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleSystem(Exception ex) {
        log.error("未处理的系统异常", ex);
        return ApiResponse.failure(ErrorCode.SYSTEM_ERROR.getCode(), "服务器内部错误");
    }
}

