package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 公众号 access_token 响应。
 * 当前项目已经统一改用 WechatAccessTokenResponse，这个类保留是为了兼容旧代码或后续迁移。
 */
@Data
public class OfficialAccountAccessTokenResponse {
    /** 公众号 access_token。 */
    @JsonProperty("access_token")
    private String accessToken;

    /** token 有效期，单位秒。 */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /** 微信错误码。 */
    @JsonProperty("errcode")
    private Integer errCode;

    /** 微信错误信息。 */
    @JsonProperty("errmsg")
    private String errMsg;
}
