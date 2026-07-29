package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信 generate_urllink 接口响应。
 */
@Data
public class WechatUrlLinkResponse {

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    /** 生成的小程序短链接。 */
    @JsonProperty("url_link")
    private String urlLink;
}
