package com.record.integration.wechat;

import com.record.common.config.AppProperties;
import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.AuthException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 小程序订阅消息客户端。
 * 负责缓存 access_token 并调用微信小程序订阅消息发送接口。
 */
@Component
public class MiniProgramMessageClient {

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

    /**
     * 发送小程序订阅消息。
     */
    public void sendSubscribeMessage(MiniProgramMessageRequest request) {
        String accessToken = getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getReminder().getMiniProgram().getSendUrl())
                .queryParam("access_token", accessToken)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        WechatMessageResponse response = restTemplate.postForObject(
                url,
                new HttpEntity<>(request, headers),
                WechatMessageResponse.class
        );
        if (response == null || response.getErrCode() == null || response.getErrCode() != 0) {
            throw new AuthException("小程序订阅消息发送失败");
        }
    }

    /**
     * 获取并缓存小程序全局 access_token。
     */
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
            throw new AuthException("小程序 access_token 获取失败");
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
