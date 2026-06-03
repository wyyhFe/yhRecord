package com.record.modules.auth.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.auth.model.dto.RefreshTokenRequest;
import com.record.modules.auth.model.dto.WxLoginRequest;
import com.record.modules.auth.model.vo.AuthTokenVO;
import com.record.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 认证接口。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "微信登录")
    @PostMapping("/wx-login")
    public ApiResponse<AuthTokenVO> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.success(authService.wxLogin(request.getCode()));
    }

    @Operation(summary = "刷新 token")
    @PostMapping("/refresh")
    public ApiResponse<AuthTokenVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request.getRefreshToken()));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout(UserContext.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "获取当前用户 ID")
    @GetMapping("/me")
    public ApiResponse<Long> me() {
        return ApiResponse.success(UserContext.getUserId());
    }

    // ==================== OAuth 第三方登录 ====================

    private static final String FRONTEND_OAUTH_CALLBACK = "http://localhost:8849/#/auth/callback";

    @Operation(summary = "OAuth 授权跳转")
    @GetMapping("/{provider}/authorize")
    public void oauthAuthorize(@PathVariable String provider, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "");
        String authorizeUrl = authService.buildOAuthAuthorizeUrl(provider, state);
        response.sendRedirect(authorizeUrl);
    }

    @Operation(summary = "OAuth 回调处理")
    @GetMapping("/{provider}/callback")
    public void oauthCallback(@PathVariable String provider,
                              @RequestParam String code,
                              @RequestParam(required = false) String state,
                              HttpServletResponse response) throws IOException {
        AuthTokenVO token = authService.oauthLogin(provider, code);
        // 签发成功后重定向回前端，将 token 信息附加在 URL 参数里
        String callbackUrl = FRONTEND_OAUTH_CALLBACK
                + "?accessToken=" + URLEncoder.encode(token.getAccessToken(), StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8)
                + "&userId=" + token.getUserId()
                + "&provider=" + provider;
        response.sendRedirect(callbackUrl);
    }
}
