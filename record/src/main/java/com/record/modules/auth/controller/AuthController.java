package com.record.modules.auth.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.auth.dto.RefreshTokenRequest;
import com.record.modules.auth.dto.WxLoginRequest;
import com.record.modules.auth.service.AuthService;
import com.record.modules.auth.vo.AuthTokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证模块控制器。
 * 负责处理微信登录、刷新 token、退出登录以及当前会话查询。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 使用小程序 code 进行登录，首次登录会自动创建业务用户。
     */
    @Operation(summary = "微信登录")
    @PostMapping("/wx-login")
    public ApiResponse<AuthTokenVO> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.success(authService.wxLogin(request.getCode()));
    }

    /**
     * 使用 refreshToken 刷新当前会话。
     */
    @Operation(summary = "刷新 token")
    @PostMapping("/refresh")
    public ApiResponse<AuthTokenVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request.getRefreshToken()));
    }

    /**
     * 退出当前会话。
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout(UserContext.getUserId());
        return ApiResponse.success();
    }

    /**
     * 返回当前登录用户 ID，用于前端快速确认登录态。
     */
    @Operation(summary = "获取当前用户 ID")
    @GetMapping("/me")
    public ApiResponse<Long> me() {
        return ApiResponse.success(UserContext.getUserId());
    }
}
