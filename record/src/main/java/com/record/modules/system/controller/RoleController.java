package com.record.modules.system.controller;

import com.record.common.model.ApiResponse;
import com.record.modules.system.model.entity.Role;
import com.record.modules.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口。
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "获取所有角色")
    @GetMapping("/list")
    public ApiResponse<List<Role>> list() {
        return ApiResponse.success(roleService.listAll());
    }

    @Operation(summary = "创建或更新角色")
    @PostMapping("/save")
    public ApiResponse<Role> save(@RequestBody Role role) {
        return ApiResponse.success(roleService.saveOrUpdateRole(role));
    }

    @Operation(summary = "分配角色菜单权限")
    @PostMapping("/assign-menus")
    public ApiResponse<Void> assignMenus(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return ApiResponse.success();
    }
}
