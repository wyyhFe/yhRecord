package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 小程序订阅消息发送请求体。
 * 字段命名需要严格对齐微信接口，所以通过 JsonProperty 映射成微信要求的参数名。
 */
@Data
@Builder
public class MiniProgramMessageRequest {
    /** 接收订阅消息的小程序用户 openid。 */
    @JsonProperty("touser")
    private String toUser;

    /** 微信后台配置的订阅消息模板 ID。 */
    @JsonProperty("template_id")
    private String templateId;

    /** 用户点击消息后跳转的小程序页面路径。 */
    private String page;

    /** 模板数据，key 需要与微信模板字段名一致。 */
    private Map<String, TemplateData> data;

    /** 小程序版本状态，developer / trial / formal。 */
    @JsonProperty("miniprogram_state")
    private String miniProgramState;

    /** 模板消息语言，通常使用 zh_CN。 */
    private String lang;

    /**
     * 单个模板字段的数据结构。
     * value 就是最终显示在消息里的文本。
     */
    @Data
    @Builder
    public static class TemplateData {
        private String value;
    }
}
