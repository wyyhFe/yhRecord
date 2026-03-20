package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信 access_token 获取响应。
 * 小程序订阅消息和公众号模板消息都会先获取各自的 access_token。
 */
@Data
public class WechatAccessTokenResponse {
    /** 微信返回的 access_token。 */
    @JsonProperty("access_token")
    private String accessToken;

    /** token 有效期，单位秒。 */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /** 微信错误码。 */
    @JsonProperty("errcode")
    private Integer errCode;

    /** 微信错误信息。 */
    @JsonProperty("errmsg")
    private String errMsg;
}
