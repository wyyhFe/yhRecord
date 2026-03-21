package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 公众号模板消息请求体。
 */
@Data
@Builder
public class OfficialMessageRequest {

    @JsonProperty("touser")
    private String toUser;

    @JsonProperty("template_id")
    private String templateId;

    private String url;

    /**
     * 模板数据，key 必须和微信模板字段名一致。
     */
    private Map<String, TemplateData> data;

    @Data
    @Builder
    public static class TemplateData {
        private String value;
    }
}
