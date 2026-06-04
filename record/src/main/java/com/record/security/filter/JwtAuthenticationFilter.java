package com.record.security.filter;

import com.record.common.constant.RedisKeyConstants;
import com.record.common.context.UserContext;
import com.record.common.exception.AuthException;
import com.record.security.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器。
 * 用于校验访问令牌，并检查当前登录会话是否仍然有效。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String[] WHITE_LIST = {
            "/auth/wx-login",
            "/auth/refresh",
            "/auth/github/**",
            "/auth/google/**",
            "/api/auth/github/**",
            "/api/auth/google/**",
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/error"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StringRedisTemplate stringRedisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String path : WHITE_LIST) {
            if (antPathMatcher.match(path, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AuthException("缺少认证令牌");
        }

        String token = authorization.substring(7);
        Claims claims = jwtTokenProvider.parseToken(token);
        if (!"access".equals(claims.get("typ", String.class))) {
            throw new AuthException("令牌类型错误");
        }

        Long userId = ((Number) claims.get("uid")).longValue();
        String openId = claims.get("openid", String.class);
        String sessionId = claims.get("sid", String.class);
        String currentSession = stringRedisTemplate.opsForValue().get(RedisKeyConstants.USER_SESSION + userId);
        if (currentSession == null || !currentSession.equals(sessionId)) {
            throw new AuthException("登录状态已失效");
        }

        UserContext.set(userId, openId, sessionId);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userId, null, AuthorityUtils.NO_AUTHORITIES
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
