package com.record.modules.security.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 图片安全检测请求体。
 */
@Schema(description = "图片安全检测请求")
public class ImageCheckRequest {

    @Schema(description = "图片 URL（OSS 上的可访问地址）", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.oss-cn-hangzhou.aliyuncs.com/diary/20260321/abc.jpg")
    @NotBlank
    private String mediaUrl;

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }
}
