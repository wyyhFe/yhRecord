package com.record.integration.oauth.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.record.common.config.AppProperties;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.integration.oauth.OAuthProvider;
import com.record.integration.oauth.OAuthUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Google OAuth2 登录实现。
 *
 * <p>申请地址：https://console.cloud.google.com/apis/credentials</p>
 */
@Component
public class GoogleOAuthProvider implements OAuthProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthProvider.class);

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private final AppProperties.OAuth.Google config;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleOAuthProvider(AppProperties appProperties) {
        this.config = appProperties.getOauth().getGoogle();
    }

    @Override
    public String getProviderName() {
        return "google";
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        return AUTHORIZE_URL
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&state=" + state
                + "&scope=openid email profile"
                + "&access_type=offline";
    }

    @Override
    public OAuthUserInfo handleCallback(String code) {
        try {
            // 1. 用 code 换 access_token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", config.getClientId());
            params.add("client_secret", config.getClientSecret());
            params.add("code", code);
            params.add("redirect_uri", config.getRedirectUri());
            params.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
            ResponseEntity<String> tokenResponse = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, tokenRequest, String.class);

            JsonNode tokenNode = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenNode.get("access_token").asText();

            log.info("[Google OAuth] 成功获取 access_token");

            // 2. 用 access_token 获取用户信息
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);

            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
            ResponseEntity<String> userResponse = restTemplate.exchange(USER_URL, HttpMethod.GET, userRequest, String.class);

            JsonNode userNode = objectMapper.readTree(userResponse.getBody());

            return OAuthUserInfo.builder()
                    .providerUserId(userNode.has("id") ? userNode.get("id").asText() : userNode.get("sub").asText())
                    .nickname(userNode.has("name") ? userNode.get("name").asText() : null)
                    .avatarUrl(userNode.has("picture") ? userNode.get("picture").asText() : null)
                    .build();
        } catch (Exception e) {
            log.error("[Google OAuth] 回调处理失败", e);
            throw new BusinessException(ErrorCode.AUTH_ERROR, "Google 登录失败: " + e.getMessage());
        }
    }
}
