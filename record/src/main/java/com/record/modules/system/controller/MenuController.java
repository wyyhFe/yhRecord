package com.record.modules.system.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.system.model.entity.Menu;
import com.record.modules.system.model.vo.AsyncRouteVO;
import com.record.modules.system.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理接口。
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "获取动态路由")
    @GetMapping("/get-async-routes")
    public ApiResponse<List<AsyncRouteVO>> getAsyncRoutes() {
        Long userId = UserContext.getUserId();
        List<AsyncRouteVO> routes = menuService.getAsyncRoutesByUserId(userId);
        return ApiResponse.success(routes);
    }

    @Operation(summary = "获取所有菜单列表")
    @GetMapping("/list")
    public ApiResponse<List<Menu>> list() {
        return ApiResponse.success(menuService.listAllEnabled());
    }

    @Operation(summary = "创建或更新菜单")
    @PostMapping("/save")
    public ApiResponse<Menu> save(@RequestBody Menu menu) {
        return ApiResponse.success(menuService.saveOrUpdateMenu(menu));
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success();
    }
}
