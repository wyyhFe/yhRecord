package com.record.integration.wechat;

import com.record.common.config.AppProperties;
import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信 URL Link 客户端。
 * 用于生成云调用短链接，微信扫一扫打开后可直接唤起小程序。
 * access_token 复用 {@link MiniProgramMessageClient} 的缓存。
 */
@Component
public class WechatUrlLinkClient {

    private static final Logger log = LoggerFactory.getLogger(WechatUrlLinkClient.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public WechatUrlLinkClient(RestTemplate restTemplate,
                               AppProperties appProperties,
                               StringRedisTemplate stringRedisTemplate) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成一个打开小程序指定页面并携带参数的短链接。
     *
     * @param path  小程序页面路径，如 "pages/home/index"
     * @param query URL query 参数，如 "qrToken=abc123"
     * @return 短链接，如 "https://wxaurl.cn/xxx"
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
                "expire_interval", 1  // 1 天后过期
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        WechatUrlLinkResponse response;
        try {
            response = restTemplate.postForObject(url, request, WechatUrlLinkResponse.class);
        } catch (Exception e) {
            log.error("[WechatUrlLink] 生成 URL Link 失败: {}", e.getMessage());
            throw new AuthException("生成小程序链接失败");
        }

        if (response == null || response.getErrCode() == null || response.getErrCode() != 0) {
            String errMsg = response != null ? response.getErrMsg() : "无响应";
            log.error("[WechatUrlLink] 微信返回错误 errcode={} errmsg={}",
                    response != null ? response.getErrCode() : null, errMsg);
            throw new AuthException("生成小程序链接失败: " + errMsg);
        }

        log.info("[WechatUrlLink] 生成短链接成功 path={} query={} link={}", path, query, response.getUrlLink());
        return response.getUrlLink();
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
