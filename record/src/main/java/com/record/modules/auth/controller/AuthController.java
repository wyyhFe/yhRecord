package com.record.modules.auth.controller;

import com.record.common.config.AppProperties;
import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.auth.model.OAuthStateContext;
import com.record.modules.auth.model.dto.AdminLoginRequest;
import com.record.modules.auth.model.dto.AdminRegisterRequest;
import com.record.modules.auth.model.dto.RefreshTokenRequest;
import com.record.modules.auth.model.dto.WxLoginRequest;
import com.record.modules.auth.model.vo.AuthTokenVO;
import com.record.modules.auth.model.vo.OAuthAuthorizeUrlVO;
import com.record.modules.auth.service.AuthService;
import com.record.modules.auth.service.OAuthStateService;
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

/**
 * 认证接口。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final OAuthStateService oAuthStateService;
    private final AppProperties appProperties;

    public AuthController(AuthService authService,
                          OAuthStateService oAuthStateService,
                          AppProperties appProperties) {
        this.authService = authService;
        this.oAuthStateService = oAuthStateService;
        this.appProperties = appProperties;
    }

    @Operation(summary = "管理后台登录（用户名 + 密码）")
    @PostMapping("/login")
    public ApiResponse<AuthTokenVO> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(authService.adminLogin(request.getUsername(), request.getPassword()));
    }

    @Operation(summary = "管理后台注册（创建本地账号，自动赋予 admin 角色）")
    @PostMapping("/register")
    public ApiResponse<AuthTokenVO> register(@Valid @RequestBody AdminRegisterRequest request) {
        return ApiResponse.success(
                authService.adminRegister(request.getUsername(), request.getPassword()));
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

    @Operation(summary = "OAuth 登录授权跳转")
    @GetMapping("/{provider}/authorize")
    public void oauthAuthorize(@PathVariable String provider, HttpServletResponse response) throws IOException {
        String state = oAuthStateService.issueLoginState(provider);
        String authorizeUrl = authService.buildOAuthAuthorizeUrl(provider, state);
        response.sendRedirect(authorizeUrl);
    }

    @Operation(summary = "OAuth 绑定授权 URL（已登录态发起绑定）")
    @GetMapping("/{provider}/bind/authorize")
    public ApiResponse<OAuthAuthorizeUrlVO> oauthBindAuthorize(@PathVariable String provider) {
        String state = oAuthStateService.issueBindState(provider, UserContext.getUserId());
        String authorizeUrl = authService.buildOAuthAuthorizeUrl(provider, state);
        return ApiResponse.success(OAuthAuthorizeUrlVO.builder().authorizeUrl(authorizeUrl).build());
    }

    @Operation(summary = "OAuth 回调处理（登录/绑定统一入口，按 state 意图分流）")
    @GetMapping("/{provider}/callback")
    public void oauthCallback(@PathVariable String provider,
                              @RequestParam String code,
                              @RequestParam(required = false) String state,
                              HttpServletResponse response) throws IOException {
        OAuthStateContext context = oAuthStateService.consume(state);
        if (!provider.equalsIgnoreCase(context.getProvider())) {
            throw new IllegalArgumentException("回调路径与 state 中记录的 provider 不一致");
        }

        if (context.getIntent() == OAuthStateContext.Intent.BIND) {
            authService.bindIdentity(context.getUserId(), provider, code);
            response.sendRedirect(buildBindResultUrl(provider, "success"));
            return;
        }

        // 登录流程：换取 token 后跳转回前端登录回调页
        AuthTokenVO token = authService.oauthLogin(provider, code);
        StringBuilder callbackUrl = new StringBuilder(appProperties.getOauth().getFrontendCallbackUrl())
                .append("?accessToken=").append(URLEncoder.encode(token.getAccessToken(), StandardCharsets.UTF_8))
                .append("&refreshToken=").append(URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8))
                .append("&userId=").append(token.getUserId())
                .append("&provider=").append(provider);
        if (token.getRoles() != null && !token.getRoles().isEmpty()) {
            callbackUrl.append("&roles=").append(URLEncoder.encode(String.join(",", token.getRoles()), StandardCharsets.UTF_8));
        }
        response.sendRedirect(callbackUrl.toString());
    }

    private String buildBindResultUrl(String provider, String status) {
        return appProperties.getOauth().getFrontendBindResultUrl()
                + "?bind=" + status
                + "&provider=" + provider;
    }
}
