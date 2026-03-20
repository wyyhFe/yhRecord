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
 * 公众号模板消息客户端。
 * 公众号提醒是扩展通道，依赖额外的公众号 openid 绑定能力。
 */
@Component
public class OfficialAccountClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public OfficialAccountClient(RestTemplate restTemplate,
                                 AppProperties appProperties,
                                 StringRedisTemplate stringRedisTemplate) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 发送公众号模板消息。
     */
    public void sendTemplateMessage(OfficialMessageRequest request) {
        String accessToken = getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getReminder().getOfficialAccount().getSendUrl())
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
            throw new AuthException("公众号模板消息发送失败");
        }
    }

    /**
     * 获取并缓存公众号 access_token。
     */
    private String getAccessToken() {
        String token = stringRedisTemplate.opsForValue().get(RedisKeyConstants.OFFICIAL_ACCESS_TOKEN);
        if (token != null && !token.isBlank()) {
            return token;
        }

        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getReminder().getOfficialAccount().getTokenUrl())
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", appProperties.getReminder().getOfficialAccount().getAppId())
                .queryParam("secret", appProperties.getReminder().getOfficialAccount().getAppSecret())
                .toUriString();
        WechatAccessTokenResponse response = restTemplate.getForObject(url, WechatAccessTokenResponse.class);
        if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
            throw new AuthException("公众号 access_token 获取失败");
        }

        long expireSeconds = response.getExpiresIn() == null ? 7200L : response.getExpiresIn();
        stringRedisTemplate.opsForValue().set(
                RedisKeyConstants.OFFICIAL_ACCESS_TOKEN,
                response.getAccessToken(),
                Math.max(expireSeconds - 300L, 60L),
                TimeUnit.SECONDS
        );
        return response.getAccessToken();
    }
}
