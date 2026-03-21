package com.record.security.service;

import com.record.common.config.AppProperties;
import com.record.common.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具。
 * 负责生成访问令牌、刷新令牌，并解析令牌里的载荷数据。
 */
@Component
public class JwtTokenProvider {

    private final AppProperties appProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.secretKey = Keys.hmacShaKeyFor(appProperties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String openId, String sessionId) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(appProperties.getSecurity().getJwt().getAccessTokenExpireMinutes(), ChronoUnit.MINUTES);
        return createToken(userId, openId, sessionId, "access", now, expireAt);
    }

    public String createRefreshToken(Long userId, String openId, String sessionId) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(appProperties.getSecurity().getJwt().getRefreshTokenExpireDays(), ChronoUnit.DAYS);
        return createToken(userId, openId, sessionId, "refresh", now, expireAt);
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            throw new AuthException("令牌非法或已过期");
        }
    }

    private String createToken(Long userId, String openId, String sessionId, String tokenType, Instant now, Instant expireAt) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("openid", openId);
        claims.put("sid", sessionId);
        claims.put("typ", tokenType);
        return Jwts.builder()
                .issuer(appProperties.getSecurity().getJwt().getIssuer())
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }
}
