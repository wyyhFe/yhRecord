package com.record.modules.user.controller;

import com.record.common.context.UserContext;
import com.record.common.enums.LoginType;
import com.record.common.model.ApiResponse;
import com.record.modules.user.model.vo.UserIdentityVO;
import com.record.modules.user.service.UserIdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户的第三方账号绑定管理接口。
 */
@Tag(name = "账号绑定")
@RestController
@RequestMapping("/user/identities")
public class UserIdentityController {

    private final UserIdentityService userIdentityService;

    public UserIdentityController(UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
    }

    @Operation(summary = "当前用户已绑定的第三方账号列表")
    @GetMapping
    public ApiResponse<List<UserIdentityVO>> list() {
        return ApiResponse.success(userIdentityService.listByUserId(UserContext.getUserId()));
    }

    @Operation(summary = "解绑指定第三方账号（要求剩余至少一个绑定）")
    @DeleteMapping("/{provider}")
    public ApiResponse<Void> unbind(@PathVariable LoginType provider) {
        userIdentityService.unbind(UserContext.getUserId(), provider);
        return ApiResponse.success();
    }

    @Operation(summary = "通过小程序 code 绑定微信账号")
    @PostMapping("/wechat/bind")
    public ApiResponse<Void> bindWechat(@RequestParam @NotBlank String code) {
        userIdentityService.bindWechatByCode(UserContext.getUserId(), code);
        return ApiResponse.success();
    }
}
