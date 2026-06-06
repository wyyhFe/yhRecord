package com.record.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.CommonStatus;
import com.record.common.model.ApiResponse;
import com.record.modules.system.model.dto.UserMergeRequest;
import com.record.modules.system.model.entity.UserRole;
import com.record.modules.system.service.AdminUserIdentityService;
import com.record.modules.system.service.RoleService;
import com.record.modules.system.service.UserMergeService;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.vo.UserIdentityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台用户管理接口。
 */
@Tag(name = "后台用户管理")
@RestController
@RequestMapping("/system/user")
public class SystemUserController {

    private final UserMapper userMapper;
    private final RoleService roleService;
    private final AdminUserIdentityService adminUserIdentityService;
    private final UserMergeService userMergeService;

    public SystemUserController(UserMapper userMapper,
                                RoleService roleService,
                                AdminUserIdentityService adminUserIdentityService,
                                UserMergeService userMergeService) {
        this.userMapper = userMapper;
        this.roleService = roleService;
        this.adminUserIdentityService = adminUserIdentityService;
        this.userMergeService = userMergeService;
    }

    @Operation(summary = "获取用户列表（分页）")
    @GetMapping("/list")
    public ApiResponse<Page<User>> list(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> page = userMapper.selectPage(
                new Page<>(currentPage, pageSize),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        return ApiResponse.success(page);
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(req.status());
            userMapper.updateById(user);
        }
        return ApiResponse.success();
    }

    @Operation(summary = "获取用户已分配的角色 ID 列表")
    @GetMapping("/role-ids")
    public ApiResponse<List<Long>> getRoleIds(@RequestParam Long userId) {
        List<UserRole> userRoles = roleService.getUserRoles(userId);
        return ApiResponse.success(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
    }

    @Operation(summary = "分配用户角色")
    @PostMapping("/{id}/assign-roles")
    public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        roleService.assignUserRoles(id, roleIds);
        return ApiResponse.success();
    }

    // ==================== 第三方账号绑定管理 ====================

    @Operation(summary = "查看指定用户的第三方账号绑定列表")
    @GetMapping("/{userId}/identities")
    public ApiResponse<List<UserIdentityVO>> listIdentities(@PathVariable Long userId) {
        return ApiResponse.success(adminUserIdentityService.listByUserId(userId));
    }

    @Operation(summary = "后台强制解绑指定第三方账号（不做'至少保留一个'校验）")
    @DeleteMapping("/{userId}/identities/{identityId}")
    public ApiResponse<Void> forceUnbind(@PathVariable Long userId, @PathVariable Long identityId) {
        adminUserIdentityService.forceUnbind(userId, identityId);
        return ApiResponse.success();
    }

    // ==================== 用户合并 ====================

    @Operation(summary = "合并两个用户：把 source 的全部业务数据迁移到 target，并禁用 source")
    @PostMapping("/merge")
    public ApiResponse<Void> merge(@Valid @RequestBody UserMergeRequest request) {
        userMergeService.merge(request.getSourceUserId(), request.getTargetUserId());
        return ApiResponse.success();
    }

    /** 状态更新请求体。 */
    record StatusUpdateRequest(CommonStatus status) {}
}
