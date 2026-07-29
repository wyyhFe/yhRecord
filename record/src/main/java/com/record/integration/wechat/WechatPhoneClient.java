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
 * 微信手机号客户端。
 * 负责调用微信 getuserphonenumber 接口，把小程序前端传来的 phone code 换成纯手机号。
 * access_token 缓存复用已有的 MINI_PROGRAM_ACCESS_TOKEN Redis key，与
 * {@link MiniProgramMessageClient} 共享同一个 token。
 *
 * TODO: 个人主体小程序无法使用 getPhoneNumber 接口（需政府/非营利组织/事业单位主体），
 * 当前代码保留，待主体升级后启用。
 */
@Component
public class WechatPhoneClient {

    private static final Logger log = LoggerFactory.getLogger(WechatPhoneClient.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public WechatPhoneClient(RestTemplate restTemplate,
                             AppProperties appProperties,
                             StringRedisTemplate stringRedisTemplate) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 获取用户手机号（纯数字，不含国家代码）。
     *
     * @param code 小程序端 button open-type="getPhoneNumber" 回调返回的 code
     * @return 纯手机号，如 "13800138000"
     */
    public String getPhoneNumber(String code) {
        String accessToken = getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.getWechat().getGetPhoneNumberUrl())
                .queryParam("access_token", accessToken)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("code", code), headers);

        WechatGetPhoneNumberResponse response;
        try {
            response = restTemplate.postForObject(url, request, WechatGetPhoneNumberResponse.class);
        } catch (Exception e) {
            log.error("[WechatPhone] 获取手机号请求失败: {}", e.getMessage());
            throw new AuthException("获取手机号失败，请稍后重试");
        }

        if (response == null) {
            throw new AuthException("获取手机号失败，微信未返回结果");
        }
        if (response.getErrCode() != null && response.getErrCode() != 0) {
            log.error("[WechatPhone] 微信返回错误 errcode={} errmsg={}", response.getErrCode(), response.getErrMsg());
            throw new AuthException("获取手机号失败: " + response.getErrMsg());
        }
        if (response.getPhoneInfo() == null || response.getPhoneInfo().getPurePhoneNumber() == null) {
            throw new AuthException("获取手机号失败，未获取到手机号");
        }

        String phoneNumber = response.getPhoneInfo().getPurePhoneNumber();
        log.info("[WechatPhone] 获取手机号成功 phone={}", maskPhone(phoneNumber));
        return phoneNumber;
    }

    /**
     * 获取小程序 access_token，优先从 Redis 缓存读取。
     * 缓存 key 与 {@link MiniProgramMessageClient} 共用，避免重复请求微信接口。
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

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
