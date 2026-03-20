package com.record.modules.user.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.user.dto.UserProfileUpdateRequest;
import com.record.modules.user.service.UserService;
import com.record.modules.user.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料控制器。
 * 当前只暴露资料查询和资料更新两个基础接口。
 */
@Tag(name = "用户")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取用户资料")
    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> profile() {
        return ApiResponse.success(userService.getCurrentProfile(UserContext.getUserId()));
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile/update")
    public ApiResponse<UserProfileVO> update(@RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.success(userService.updateProfile(UserContext.getUserId(), request));
    }
}
