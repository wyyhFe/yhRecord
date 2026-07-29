package com.record.integration.wechat;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * 微信内容安全检测客户端。
 * 调用 security.msgSecCheck 接口检测文本是否包含违规内容。
 * 调用 security.imgSecCheck 接口检测图片是否包含违规内容。
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/sec-check/security.msgSecCheck.html">文本检测文档</a>
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/sec-check/security.imgSecCheck.html">图片检测文档</a>
 */
@Component
public class WechatSecCheckClient {

    private static final Logger log = LoggerFactory.getLogger(WechatSecCheckClient.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public WechatSecCheckClient(RestTemplate restTemplate,
                                AppProperties appProperties,
                                StringRedisTemplate stringRedisTemplate,
                                ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 检测文本内容是否安全。
     *
     * @param content 待检测的文本内容
     * @param scene   场景值，1-资料，2-评论，3-论坛，4-社交日志
     * @param openid  用户 openid（可选，用于提高检测准确率）
     * @return 检测结果，true 表示内容安全，false 表示内容违规
     */
    public boolean checkTextSecurity(String content, int scene, String openid) {
        if (content == null || content.isBlank()) {
            return true;
        }

        String accessToken = getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/wxa/msg_sec_check")
                .queryParam("access_token", accessToken)
                .toUriString();

        SecCheckRequest request = new SecCheckRequest();
        request.setContent(content);
        request.setScene(scene);
        request.setVersion(2);
        request.setOpenid(openid);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("序列化内容安全检测请求失败: {}", e.getMessage());
            throw new AuthException("内容安全检测失败: 序列化请求失败");
        }

        log.debug(">> 微信内容安全检测 content.length={} scene={}", content.length(), scene);

        HttpResponse httpResponse;
        try {
            httpResponse = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .timeout(10000)
                    .execute();
        } catch (Exception e) {
            log.error("调用微信内容安全检测接口失败: {}", e.getMessage());
            // 网络异常时，为避免阻断用户正常使用，默认放行
            return true;
        }

        int statusCode = httpResponse.getStatus();
        String rawBody = httpResponse.body();
        log.debug("<< 微信内容安全检测响应 HTTP {} rawBody={}", statusCode, rawBody);

        if (statusCode != 200 || rawBody == null || rawBody.isBlank()) {
            log.error("微信内容安全检测失败: HTTP {}", statusCode);
            // 接口异常时，默认放行
            return true;
        }

        SecCheckResponse response;
        try {
            response = objectMapper.readValue(rawBody, SecCheckResponse.class);
        } catch (Exception e) {
            log.error("解析微信内容安全检测响应失败: {}", e.getMessage());
            // 解析失败时，默认放行
            return true;
        }

        // errcode 为 0 表示检测通过
        if (response.getErrCode() != null && response.getErrCode() != 0) {
            log.warn("微信内容安全检测返回错误: errcode={} errmsg={}", response.getErrCode(), response.getErrMsg());
            // 非业务错误（如 token 过期），默认放行
            return true;
        }

        // result.label 为空或为 100 表示内容安全
        SecCheckResult result = response.getResult();
        if (result == null) {
            return true;
        }

        // label: 100-正常，10001-广告，10002-政治，10003-色情，10004-辱骂，10005-暴恐，10006-违法，10007-欺诈，10008-杂糅
        Integer label = result.getLabel();
        if (label == null || label == 100) {
            return true;
        }

        log.warn("内容安全检测不通过: label={} suggestion={}", label, result.getSuggest());
        return false;
    }

    /**
     * 检测图片内容是否安全。
     * 通过图片 URL 调用微信 imgSecCheck 接口进行检测。
     *
     * @param mediaUrl 图片的可访问 URL（通常是 OSS 上的地址）
     * @param openid  用户 openid（可选，用于提高检测准确率）
     * @return 检测结果，true 表示图片安全，false 表示图片违规
     */
    public boolean checkImageSecurity(String mediaUrl, String openid) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            return true;
        }

        String accessToken = getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/wxa/img_sec_check")
                .queryParam("access_token", accessToken)
                .toUriString();

        ImgSecCheckRequest request = new ImgSecCheckRequest();
        request.setMediaUrl(mediaUrl);
        request.setMediaType(2);
        request.setOpenid(openid);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("序列化图片安全检测请求失败: {}", e.getMessage());
            throw new AuthException("图片安全检测失败: 序列化请求失败");
        }

        log.debug(">> 微信图片安全检测 mediaUrl={}", mediaUrl);

        HttpResponse httpResponse;
        try {
            httpResponse = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .timeout(10000)
                    .execute();
        } catch (Exception e) {
            log.error("调用微信图片安全检测接口失败: {}", e.getMessage());
            // 网络异常时，为避免阻断用户正常使用，默认放行
            return true;
        }

        int statusCode = httpResponse.getStatus();
        String rawBody = httpResponse.body();
        log.debug("<< 微信图片安全检测响应 HTTP {} rawBody={}", statusCode, rawBody);

        if (statusCode != 200 || rawBody == null || rawBody.isBlank()) {
            log.error("微信图片安全检测失败: HTTP {}", statusCode);
            // 接口异常时，默认放行
            return true;
        }

        SecCheckResponse response;
        try {
            response = objectMapper.readValue(rawBody, SecCheckResponse.class);
        } catch (Exception e) {
            log.error("解析微信图片安全检测响应失败: {}", e.getMessage());
            // 解析失败时，默认放行
            return true;
        }

        // errcode 为 0 表示检测通过
        if (response.getErrCode() != null && response.getErrCode() != 0) {
            log.warn("微信图片安全检测返回错误: errcode={} errmsg={}", response.getErrCode(), response.getErrMsg());
            // 非业务错误（如 token 过期），默认放行
            return true;
        }

        // result.label 为空或为 100 表示图片安全
        SecCheckResult result = response.getResult();
        if (result == null) {
            return true;
        }

        // label: 100-正常，10001-广告，10002-政治，10003-色情，10004-辱骂，10005-暴恐，10006-违法，10007-欺诈，10008-杂糅
        Integer label = result.getLabel();
        if (label == null || label == 100) {
            return true;
        }

        log.warn("图片安全检测不通过: label={} suggestion={}", label, result.getSuggest());
        return false;
    }

    /**
     * 获取小程序 access_token。
     * 优先从 Redis 缓存获取，缓存不存在或过期则重新请求微信接口。
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

    /**
     * 内容安全检测请求体。
     */
    public static class SecCheckRequest {
        @JsonProperty("content")
        private String content;

        @JsonProperty("scene")
        private Integer scene;

        @JsonProperty("version")
        private Integer version;

        @JsonProperty("openid")
        private String openid;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Integer getScene() { return scene; }
        public void setScene(Integer scene) { this.scene = scene; }
        public Integer getVersion() { return version; }
        public void setVersion(Integer version) { this.version = version; }
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
    }

    /**
     * 图片安全检测请求体。
     */
    public static class ImgSecCheckRequest {
        @JsonProperty("media_url")
        private String mediaUrl;

        @JsonProperty("media_type")
        private Integer mediaType;

        @JsonProperty("openid")
        private String openid;

        public String getMediaUrl() { return mediaUrl; }
        public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
        public Integer getMediaType() { return mediaType; }
        public void setMediaType(Integer mediaType) { this.mediaType = mediaType; }
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
    }

    /**
     * 内容安全检测响应体。
     */
    public static class SecCheckResponse {
        @JsonProperty("errcode")
        private Integer errCode;

        @JsonProperty("errmsg")
        private String errMsg;

        @JsonProperty("result")
        private SecCheckResult result;

        public Integer getErrCode() { return errCode; }
        public void setErrCode(Integer errCode) { this.errCode = errCode; }
        public String getErrMsg() { return errMsg; }
        public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
        public SecCheckResult getResult() { return result; }
        public void setResult(SecCheckResult result) { this.result = result; }
    }

    /**
     * 检测结果详情。
     */
    public static class SecCheckResult {
        @JsonProperty("suggest")
        private String suggest;

        @JsonProperty("label")
        private Integer label;

        public String getSuggest() { return suggest; }
        public void setSuggest(String suggest) { this.suggest = suggest; }
        public Integer getLabel() { return label; }
        public void setLabel(Integer label) { this.label = label; }
    }
}