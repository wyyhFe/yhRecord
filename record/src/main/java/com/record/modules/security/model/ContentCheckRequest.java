package com.record.modules.security.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "内容安全检测请求")
public class ContentCheckRequest {

    @Schema(description = "待检测的文本内容", required = true)
    private String content;

    @Schema(description = "场景值：1-资料，2-评论，3-论坛，4-社交日志", example = "4")
    private Integer scene = 4;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getScene() {
        return scene;
    }

    public void setScene(Integer scene) {
        this.scene = scene;
    }
}