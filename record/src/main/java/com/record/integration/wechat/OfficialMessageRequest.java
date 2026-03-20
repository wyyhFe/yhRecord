package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OfficialMessageRequest {
    @JsonProperty("touser")
    private String toUser;
    @JsonProperty("template_id")
    private String templateId;
    private String url;
    private Map<String, TemplateData> data;

    @Data
    @Builder
    public static class TemplateData {
        private String value;
    }
}
