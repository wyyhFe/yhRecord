package com.record.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.modules.system.mapper.MenuMapper;
import com.record.modules.system.model.entity.Menu;
import com.record.modules.system.model.vo.AsyncRouteVO;
import com.record.modules.system.service.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuServiceImpl 单元测试")
class MenuServiceImplTest {

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MenuServiceImpl menuService;

    private Menu createMenu(Long id, Long parentId, String name, String path,
                            String component, String menuType, Map<String, Object> meta) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setName(name);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setMenuType(menuType);
        menu.setMeta(meta);
        menu.setStatus(CommonStatus.ENABLED);
        return menu;
    }

    @Nested
    @DisplayName("getAsyncRoutesByUserId 方法")
    class GetAsyncRoutesTests {

        @Test
        @DisplayName("构建树形路由：父目录 + 子页面")
        void buildRouteTree_parentWithChildren() {
            Long userId = 1L;
            when(roleService.getRoleNamesByUserId(userId)).thenReturn(List.of("admin"));

            Menu dir = createMenu(1L, null, "Business", "/business",
                    null, "DIRECTORY", Map.of("title", "业务管理", "rank", 10));
            Menu page = createMenu(2L, 1L, "Diary", "/business/diary",
                    "business/diary/index", "PAGE",
                    Map.of("title", "日记管理", "rank", 1, "showLink", true, "roles", List.of("admin")));

            when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(dir, page));

            List<AsyncRouteVO> routes = menuService.getAsyncRoutesByUserId(userId);

            assertEquals(1, routes.size());
            AsyncRouteVO parent = routes.get(0);
            assertEquals("/business", parent.getPath());
            assertNull(parent.getComponent()); // 目录没有 component
            assertNotNull(parent.getChildren());
            assertEquals(1, parent.getChildren().size());
            assertEquals("business/diary/index", parent.getChildren().get(0).getComponent());
        }

        @Test
        @DisplayName("角色过滤：无权限菜单不返回")
        void roleFilter_excludesUnauthorized() {
            Long userId = 1L;
            when(roleService.getRoleNamesByUserId(userId)).thenReturn(List.of("editor"));

            Menu page = createMenu(1L, null, "Diary", "/diary",
                    "business/diary/index", "PAGE",
                    Map.of("title", "日记管理", "roles", List.of("admin")));

            when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(page));

            List<AsyncRouteVO> routes = menuService.getAsyncRoutesByUserId(userId);

            assertTrue(routes.isEmpty());
        }

        @Test
        @DisplayName("无角色限制的菜单所有用户可见")
        void noRoleRestriction_visibleToAll() {
            Long userId = 1L;
            when(roleService.getRoleNamesByUserId(userId)).thenReturn(List.of("editor"));

            Menu page = createMenu(1L, null, "Home", "/",
                    "dashboard/index", "PAGE", Map.of("title", "首页"));

            when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(page));

            List<AsyncRouteVO> routes = menuService.getAsyncRoutesByUserId(userId);

            assertEquals(1, routes.size());
        }

        @Test
        @DisplayName("meta 为空时仍正常返回")
        void nullMeta_handledGracefully() {
            Long userId = 1L;
            when(roleService.getRoleNamesByUserId(userId)).thenReturn(List.of("admin"));

            Menu page = createMenu(1L, null, "Test", "/test",
                    "test/index", "PAGE", null);

            when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(page));

            List<AsyncRouteVO> routes = menuService.getAsyncRoutesByUserId(userId);

            assertEquals(1, routes.size());
            assertNull(routes.get(0).getMeta().getTitle());
        }

        @Test
        @DisplayName("多级嵌套目录正确构建")
        void nestedDirectories_builtCorrectly() {
            Long userId = 1L;
            when(roleService.getRoleNamesByUserId(userId)).thenReturn(List.of("admin"));

            Menu root = createMenu(1L, null, "System", "/system",
                    null, "DIRECTORY", Map.of("title", "系统管理"));
            Menu sub = createMenu(2L, 1L, "UserManage", "/system/user",
                    "system/user/index", "PAGE", Map.of("title", "用户管理", "roles", List.of("admin")));

            when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(root, sub));

            List<AsyncRouteVO> routes = menuService.getAsyncRoutesByUserId(userId);

            assertEquals(1, routes.size());
            assertEquals(1, routes.get(0).getChildren().size());
            assertEquals("/system/user", routes.get(0).getChildren().get(0).getPath());
        }
    }

    @Nested
    @DisplayName("listAllEnabled 方法")
    class ListAllEnabledTests {

        @Test
        @DisplayName("返回所有启用菜单")
        void listAllEnabled_returnsAll() {
            Menu menu = createMenu(1L, null, "Home", "/",
                    "dashboard/index", "PAGE", Map.of("title", "首页"));
            when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(menu));

            List<Menu> result = menuService.listAllEnabled();

            assertEquals(1, result.size());
        }
    }
}
