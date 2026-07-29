package com.record.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信小程序 getuserphonenumber 接口响应。
 */
@Data
public class WechatGetPhoneNumberResponse {

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    @JsonProperty("phone_info")
    private PhoneInfo phoneInfo;

    @Data
    public static class PhoneInfo {

        @JsonProperty("phoneNumber")
        private String phoneNumber;

        @JsonProperty("purePhoneNumber")
        private String purePhoneNumber;

        @JsonProperty("countryCode")
        private String countryCode;

        @JsonProperty("watermark")
        private Watermark watermark;
    }

    @Data
    public static class Watermark {

        @JsonProperty("timestamp")
        private Long timestamp;

        @JsonProperty("appid")
        private String appid;
    }
}
