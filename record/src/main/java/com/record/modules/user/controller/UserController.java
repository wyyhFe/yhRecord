package com.record.modules.user.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.user.model.dto.UserProfileUpdateRequest;
import com.record.modules.user.model.vo.PublicUserVO;
import com.record.modules.user.model.vo.UserProfileVO;
import com.record.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料接口。
 */
@Tag(name = "用户资料")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查询当前用户资料")
    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> profile() {
        return ApiResponse.success(userService.getCurrentProfile(UserContext.getUserId()));
    }

    @Operation(summary = "更新当前用户资料")
    @PutMapping("/profile/update")
    public ApiResponse<UserProfileVO> update(@RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.success(userService.updateProfile(UserContext.getUserId(), request));
    }

    @Operation(summary = "查看指定用户的公开资料")
    @GetMapping("/public/{userId}")
    public ApiResponse<PublicUserVO> publicProfile(@PathVariable Long userId) {
        return ApiResponse.success(userService.getPublicProfile(userId));
    }
}
