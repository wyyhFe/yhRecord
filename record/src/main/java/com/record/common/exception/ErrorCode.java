package com.record.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未登录或登录已失效"),
    FORBIDDEN(40300, "无权访问"),
    NOT_FOUND(40400, "资源不存在"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后再试"),
    AUTH_ERROR(40110, "认证失败"),
    USER_ERROR(41010, "用户信息异常"),
    DIARY_ERROR(42010, "日记模块异常"),
    TAG_ERROR(43010, "标签模块异常"),
    LEDGER_ERROR(44010, "记账模块异常"),
    CHECKIN_ERROR(45010, "打卡模块异常"),
    MEMORIAL_ERROR(46010, "纪念日模块异常"),
    LOCATION_ERROR(47010, "定位模块异常"),
    FILE_ERROR(48010, "文件模块异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
