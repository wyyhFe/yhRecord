package com.record.modules.security.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "内容安全检测结果")
public class ContentCheckResult {

    @Schema(description = "是否安全", example = "true")
    private boolean safe;

    @Schema(description = "不安全时的错误描述")
    private String message;

    public static ContentCheckResult safe() {
        ContentCheckResult result = new ContentCheckResult();
        result.setSafe(true);
        return result;
    }

    public static ContentCheckResult unsafe(String message) {
        ContentCheckResult result = new ContentCheckResult();
        result.setSafe(false);
        result.setMessage(message);
        return result;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}