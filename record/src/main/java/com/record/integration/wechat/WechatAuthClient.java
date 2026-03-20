package com.record.integration.wechat;

import com.record.common.config.AppProperties;
import com.record.common.exception.AuthException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WechatAuthClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public WechatAuthClient(RestTemplate restTemplate, AppProperties appProperties) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
    }

    public WechatCode2SessionResponse code2Session(String code) {
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getWechat().getCode2sessionUrl())
                .queryParam("appid", appProperties.getWechat().getAppId())
                .queryParam("secret", appProperties.getWechat().getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        WechatCode2SessionResponse response = restTemplate.getForObject(url, WechatCode2SessionResponse.class);
        if (response == null || response.getOpenid() == null) {
            throw new AuthException("微信登录失败");
        }
        if (response.getErrCode() != null && response.getErrCode() != 0) {
            throw new AuthException("微信登录失败: " + response.getErrMsg());
        }
        return response;
    }
}
