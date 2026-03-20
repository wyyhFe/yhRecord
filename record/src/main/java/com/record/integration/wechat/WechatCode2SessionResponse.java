package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信 code2Session 响应。
 * 小程序前端调用 wx.login 拿到 code 后，后端会用这个对象接收微信返回的 openid 等信息。
 */
@Data
public class WechatCode2SessionResponse {
    /** 小程序用户唯一标识。 */
    private String openid;

    /** 会话密钥，仅微信侧使用，业务上不直接返回前端。 */
    @JsonProperty("session_key")
    private String sessionKey;

    /** 开放平台统一标识，当前项目首版不强依赖。 */
    private String unionid;

    /** 微信错误码，成功时通常为空或为 0。 */
    @JsonProperty("errcode")
    private Integer errCode;

    /** 微信错误信息。 */
    @JsonProperty("errmsg")
    private String errMsg;
}
