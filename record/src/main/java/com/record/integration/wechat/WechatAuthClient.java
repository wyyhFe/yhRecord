package com.record.integration.wechat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.record.common.config.AppProperties;
import com.record.common.exception.AuthException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 微信登录客户端。
 * 负责调用微信 code2Session 接口，把小程序前端传来的 code 换成 openid 和 session_key。
 */
@Component
public class WechatAuthClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public WechatAuthClient(RestTemplate restTemplate, AppProperties appProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用微信 code2Session 接口。
     * 执行流程：
     * 1. 读取配置里的小程序 appId 和 appSecret。
     * 2. 拼接微信接口地址。
     * 3. 使用前端传来的 code 请求微信服务器。
     * 4. 把微信返回的 JSON 解析成对象。
     * 5. 校验错误码和 openid 是否存在。
     *
     * 这里不直接让 RestTemplate 反序列化成对象，
     * 是因为微信有时会把响应头返回成 text/plain，
     * 但响应体内容本身依然是 JSON。
     * 先按字符串读取，再手动解析会更稳。
     */
    public WechatCode2SessionResponse code2Session(String code) {
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getWechat().getCode2sessionUrl())
                .queryParam("appid", appProperties.getWechat().getAppId())
                .queryParam("secret", appProperties.getWechat().getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        String rawResponse = restTemplate.getForObject(url, String.class);
        WechatCode2SessionResponse response = parseResponse(rawResponse);

        if (response.getErrCode() != null && response.getErrCode() != 0) {
            throw new AuthException("微信登录失败: " + response.getErrMsg());
        }
        if (response.getOpenid() == null) {
            throw new AuthException("微信登录失败，未获取到 openid");
        }
        return response;
    }

    /**
     * 把微信原始响应字符串解析成 Java 对象。
     * 如果微信接口没有返回内容，或者返回的不是合法 JSON，这里会抛业务异常。
     */
    private WechatCode2SessionResponse parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AuthException("微信登录失败，微信接口未返回内容");
        }

        try {
            return objectMapper.readValue(rawResponse, WechatCode2SessionResponse.class);
        } catch (JsonProcessingException exception) {
            throw new AuthException("微信登录失败，接口返回内容无法解析");
        }
    }
}
