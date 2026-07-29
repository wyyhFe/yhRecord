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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信 URL Link 客户端。
 * POST 请求使用 Hutool（与 MiniProgramMessageClient 一致），避免 RestTemplate POST 兼容性问题。
 * access_token 复用 {@link MiniProgramMessageClient} 的缓存。
 */
@Component
public class WechatUrlLinkClient {

    private static final Logger log = LoggerFactory.getLogger(WechatUrlLinkClient.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public WechatUrlLinkClient(RestTemplate restTemplate,
                               AppProperties appProperties,
                               StringRedisTemplate stringRedisTemplate,
                               ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成一个打开小程序指定页面并携带参数的短链接。
     */
    public String generateUrlLink(String path, String query) {
        String accessToken = getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getWechat().getGenerateUrlLinkUrl())
                .queryParam("access_token", accessToken)
                .toUriString();

        Map<String, Object> body = Map.of(
                "path", path,
                "query", query,
                "env_version", "release",
                "is_expire", true,
                "expire_type", 1,
                "expire_interval", 1
        );

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new AuthException("序列化 URL Link 请求失败: " + e.getMessage());
        }

        log.info("[WechatUrlLink] >> url={} body={}", url, jsonBody);

        HttpResponse httpResponse = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .timeout(10000)
                .execute();
        int statusCode = httpResponse.getStatus();
        String rawBody = httpResponse.body();
        log.info("[WechatUrlLink] << HTTP {} rawBody={}", statusCode, rawBody);

        if (statusCode != 200 || rawBody == null || rawBody.isBlank()) {
            throw new AuthException("微信 URL Link API 返回异常: HTTP " + statusCode + ", body=" + rawBody);
        }

        try {
            WechatUrlLinkResponse response = objectMapper.readValue(rawBody, WechatUrlLinkResponse.class);
            if (response.getErrCode() == null || response.getErrCode() != 0) {
                throw new AuthException("生成小程序链接失败: errcode=" + response.getErrCode() + " " + response.getErrMsg());
            }
            log.info("[WechatUrlLink] 生成短链接成功 link={}", response.getUrlLink());
            return response.getUrlLink();
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("[WechatUrlLink] 解析响应异常: {}", e.getMessage(), e);
            throw new AuthException("生成小程序链接失败: 响应解析错误");
        }
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
