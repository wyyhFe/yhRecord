package com.record.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.record.common.exception.ErrorCode;
import com.record.common.model.ApiResponse;
import com.record.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 安全配置。
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   ObjectMapper objectMapper) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OAuth 登录链路无 token：跳转授权 + 回调
                        // 注意 `*` 是单段匹配，不会命中 `/auth/{provider}/bind/authorize`，所以绑定授权会落到 authenticated 分支
                        .requestMatchers("/auth/login", "/auth/register",
                                "/auth/wx-login", "/auth/refresh",
                                "/auth/*/authorize", "/auth/*/callback",
                                // 动态路由接口：web C 端 + admin 共用，未登录返回公开菜单，已登录按角色过滤
                                "/system/menu/get-async-routes",
                                // Web C 端公开博客接口
                                "/blog/public/**",
                                // 查询评论公开（发表评论需登录，在 controller 层校验）
                                "/blog/comments",
                                "/doc.html", "/swagger-ui/**",
                                "/swagger-resources/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        // 其余 /system/** 是管理后台接口，必须 admin
                        .requestMatchers("/system/**").hasRole("admin")
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    ApiResponse.failure(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已失效")));
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
