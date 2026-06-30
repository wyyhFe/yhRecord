package com.record.integration.wechat;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.record.common.config.AppProperties;
import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 小程序订阅消息客户端。
 */
@Component
public class MiniProgramMessageClient {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramMessageClient.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public MiniProgramMessageClient(RestTemplate restTemplate,
                                    AppProperties appProperties,
                                    StringRedisTemplate stringRedisTemplate) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void sendSubscribeMessage(MiniProgramMessageRequest request) {
        String accessToken = getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getReminder().getMiniProgram().getSendUrl())
                .queryParam("access_token", accessToken)
                .toUriString();

        String jsonBody;
        try {
            jsonBody = new ObjectMapper().writeValueAsString(request);
        } catch (Exception e) {
            throw new AuthException("序列化订阅消息请求失败: " + e.getMessage());
        }
        log.info(">> 发送小程序订阅消息 toUser={} templateId={}", request.getToUser(), request.getTemplateId());

        HttpResponse httpResponse = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .timeout(10000)
                .execute();
        int statusCode = httpResponse.getStatus();
        String rawBody = httpResponse.body();
        log.info("<< 微信响应 HTTP {} rawBody={}", statusCode, rawBody);

        if (statusCode != 200 || rawBody == null || rawBody.isBlank()) {
            throw new AuthException("小程序订阅消息发送失败: HTTP " + statusCode + ", body=" + rawBody);
        }

        WechatMessageResponse response;
        try {
            response = new ObjectMapper().readValue(rawBody, WechatMessageResponse.class);
        } catch (Exception e) {
            throw new AuthException("小程序订阅消息发送失败: HTTP " + statusCode + ", body=" + rawBody
                    + ", parseError=" + e.getMessage());
        }
        if (response.getErrCode() == null || response.getErrCode() != 0) {
            throw new AuthException("小程序订阅消息发送失败: HTTP " + statusCode
                    + ", errcode=" + response.getErrCode() + ", errmsg=" + response.getErrMsg());
        }
        log.info("<< 小程序订阅消息发送成功 errcode=0");
    }

    private String getAccessToken() {
        String token = stringRedisTemplate.opsForValue().get(RedisKeyConstants.MINI_PROGRAM_ACCESS_TOKEN);
        if (token != null && !token.isBlank()) {
            return token;
        }

        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getReminder().getMiniProgram().getTokenUrl())
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", appProperties.getWechat().getAppId())
                .queryParam("secret", appProperties.getWechat().getAppSecret())
                .toUriString();
        WechatAccessTokenResponse response = restTemplate.getForObject(url, WechatAccessTokenResponse.class);
        if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
            throw new AuthException("获取小程序 access_token 失败");
        }

        long expireSeconds = response.getExpiresIn() == null ? 7200L : response.getExpiresIn();
        stringRedisTemplate.opsForValue().set(
                RedisKeyConstants.MINI_PROGRAM_ACCESS_TOKEN,
                response.getAccessToken(),
                Math.max(expireSeconds - 300L, 60L),
                TimeUnit.SECONDS
        );
        return response.getAccessToken();
    }
}
