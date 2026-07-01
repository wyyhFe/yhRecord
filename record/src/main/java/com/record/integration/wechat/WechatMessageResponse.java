package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信消息发送响应。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatMessageResponse {
    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    @JsonProperty("msgid")
    private Long msgId;
}
