package com.record.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统异常，请稍后重试"),
    AUTH_ERROR(401, "认证失败"),
    USER_ERROR(41010, "用户业务异常"),
    DIARY_ERROR(42010, "日记业务异常"),
    TAG_ERROR(43010, "标签业务异常"),
    LEDGER_ERROR(44010, "记账业务异常"),
    CHECKIN_ERROR(45010, "打卡业务异常"),
    MEMORIAL_ERROR(46010, "纪念日业务异常"),
    LOCATION_ERROR(47010, "定位业务异常"),
    FILE_ERROR(48010, "文件业务异常"),
    KNOWLEDGE_ERROR(49010, "知识库业务异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
