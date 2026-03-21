package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信小程序 code2Session 响应。
 */
@Data
public class WechatCode2SessionResponse {
    private String openid;

    @JsonProperty("session_key")
    private String sessionKey;

    private String unionid;

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;
}
