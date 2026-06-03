package com.record.integration.oauth.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.record.common.config.AppProperties;
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
 * GitHub OAuth2 登录实现。
 *
 * <p>申请地址：https://github.com/settings/developers</p>
 */
@Component
public class GithubOAuthProvider implements OAuthProvider {

    private static final Logger log = LoggerFactory.getLogger(GithubOAuthProvider.class);

    private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_URL = "https://api.github.com/user";

    private final AppProperties.OAuth.Github config;
    private final RestTemplate restTemplate = new RestTemplate();

    public GithubOAuthProvider(AppProperties appProperties) {
        this.config = appProperties.getOauth().getGithub();
    }

    @Override
    public String getProviderName() {
        return "github";
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        return AUTHORIZE_URL
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8)
                + "&state=" + state
                + "&scope=read:user,user:email";
    }

    @Override
    public OAuthUserInfo handleCallback(String code) {
        // 1. 用 code 换 access_token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        String tokenBody = String.format(
                "{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"code\":\"%s\"}",
                config.getClientId(), config.getClientSecret(), code);

        HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, headers);
        ResponseEntity<String> tokenResponse = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, tokenRequest, String.class);

        JsonNode tokenNode = objectMapper().readTree(tokenResponse.getBody());
        String accessToken = tokenNode.get("access_token").asText();

        log.info("[GitHub OAuth] 成功获取 access_token");

        // 2. 用 access_token 获取用户信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.set("Accept", "application/json");

        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> userResponse = restTemplate.exchange(USER_URL, HttpMethod.GET, userRequest, String.class);

        JsonNode userNode = objectMapper().readTree(userResponse.getBody());

        return OAuthUserInfo.builder()
                .providerUserId(String.valueOf(userNode.get("id").asLong()))
                .nickname(userNode.has("login") ? userNode.get("login").asText() : null)
                .avatarUrl(userNode.has("avatar_url") ? userNode.get("avatar_url").asText() : null)
                .build();
    }

    private static com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}
