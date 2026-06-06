package com.record.security.filter;

import com.record.common.constant.RedisKeyConstants;
import com.record.common.context.UserContext;
import com.record.modules.system.service.RoleService;
import com.record.security.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 * <p>设计原则：过滤器**不主动抛异常**，能解出有效身份就写 SecurityContext，否则什么都不写。
 * 最终的访问决策（403/401）由 Spring Security 的过滤器链 + SecurityConfig 里的
 * authenticationEntryPoint 处理，避免 RuntimeException 绕过 entryPoint 变成 500。</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** 完全跳过此过滤器的路径（OAuth / Swagger / Spring 错误转发等）。 */
    private static final String[] WHITE_LIST = {
            "/auth/wx-login",
            "/auth/refresh",
            "/auth/*/authorize",
            "/auth/*/callback",
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/error"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;
    private final RoleService roleService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   StringRedisTemplate stringRedisTemplate,
                                   @Lazy @Autowired RoleService roleService) {
        // RoleService 用 @Lazy 避免过滤器初始化阶段触发依赖循环
        this.jwtTokenProvider = jwtTokenProvider;
        this.stringRedisTemplate = stringRedisTemplate;
        this.roleService = roleService;
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
            // 无 token：放行过滤器，由 Spring Security 在 authorize 阶段判定（命中受保护资源走 entryPoint 出 401）
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticate(authorization.substring(7));
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // 解析/会话校验失败：清空上下文，让 Security 继续按"未认证"处理
            log.debug("[jwt-filter] 令牌校验失败，按未登录处理: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void authenticate(String token) {
        Claims claims = jwtTokenProvider.parseToken(token);
        if (!"access".equals(claims.get("typ", String.class))) {
            return;
        }

        Long userId = ((Number) claims.get("uid")).longValue();
        String openId = claims.get("openid", String.class);
        String sessionId = claims.get("sid", String.class);

        // 会话存 Redis Hash：sid + refreshToken；这里只验 sid 是否还匹配
        Object currentSid = stringRedisTemplate.opsForHash().get(RedisKeyConstants.USER_SESSION + userId, "sid");
        if (currentSid == null || !sessionId.equals(currentSid.toString())) {
            return;
        }

        UserContext.set(userId, openId, sessionId);

        // 实时查角色：避免角色变更后用户仍能用旧 token 进受保护接口
        List<GrantedAuthority> authorities = roleService.getRoleNamesByUserId(userId).stream()
                .map(name -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + name))
                .toList();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userId, null, authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
