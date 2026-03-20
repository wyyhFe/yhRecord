package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信消息发送通用响应。
 */
@Data
public class WechatMessageResponse {
    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;
}
