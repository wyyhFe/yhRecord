package com.record.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.modules.system.mapper.MenuMapper;
import com.record.modules.system.mapper.RoleMenuMapper;
import com.record.modules.system.mapper.UserRoleMapper;
import com.record.modules.system.model.entity.Menu;
import com.record.modules.system.model.entity.RoleMenu;
import com.record.modules.system.model.entity.UserRole;
import com.record.modules.system.model.vo.AsyncRouteVO;
import com.record.modules.system.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现。
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(MenuMapper menuMapper, UserRoleMapper userRoleMapper, RoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<AsyncRouteVO> getAsyncRoutesByUserId(Long userId) {
        // 1. 查用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());

        // 2. admin 角色直接返回所有菜单
        boolean isAdmin = roleIds.contains(1L);
        List<Menu> menus;
        if (isAdmin) {
            menus = menuMapper.selectList(
                    new LambdaQueryWrapper<Menu>()
                            .eq(Menu::getStatus, CommonStatus.ENABLED)
                            .ne(Menu::getMenuType, "BUTTON")
                            .orderByAsc(Menu::getRank));
        } else {
            // 3. 非管理员：根据角色查菜单
            List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
            if (menuIds.isEmpty()) {
                return Collections.emptyList();
            }
            menus = menuMapper.selectList(
                    new LambdaQueryWrapper<Menu>()
                            .eq(Menu::getStatus, CommonStatus.ENABLED)
                            .ne(Menu::getMenuType, "BUTTON")
                            .in(Menu::getId, menuIds)
                            .orderByAsc(Menu::getRank));
        }

        // 4. 构建路由树
        return buildRouteTree(menus, null);
    }

    @Override
    public List<Menu> listAllEnabled() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getStatus, CommonStatus.ENABLED)
                        .orderByAsc(Menu::getRank));
    }

    @Override
    public Menu saveOrUpdateMenu(Menu menu) {
        if (menu.getId() == null) {
            menuMapper.insert(menu);
        } else {
            menuMapper.updateById(menu);
        }
        return menu;
    }

    @Override
    public void deleteMenu(Long menuId) {
        menuMapper.deleteById(menuId);
    }

    /**
     * 将扁平菜单列表构建成树形路由结构。
     */
    private List<AsyncRouteVO> buildRouteTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .map(m -> {
                    AsyncRouteVO.AsyncRouteVOBuilder builder = AsyncRouteVO.builder()
                            .path(m.getPath())
                            .name(m.getName());

                    // 目录类型没有 component
                    if (!"DIRECTORY".equals(m.getMenuType()) && m.getComponent() != null) {
                        builder.component(m.getComponent());
                    }

                    if (m.getRedirect() != null) {
                        builder.redirect(m.getRedirect());
                    }

                    // 构建 meta
                    AsyncRouteVO.MetaVO.MetaVOBuilder metaBuilder = AsyncRouteVO.MetaVO.builder()
                            .title(m.getTitle());
                    if (m.getIcon() != null) {
                        metaBuilder.icon(m.getIcon());
                    }
                    if (m.getRank() != null && m.getRank() > 0) {
                        metaBuilder.rank(m.getRank());
                    }
                    if (m.getShowLink() != null && !m.getShowLink()) {
                        metaBuilder.showLink(false);
                    }
                    if (m.getKeepAlive() != null && m.getKeepAlive()) {
                        metaBuilder.keepAlive(true);
                    }
                    if (m.getFrameSrc() != null) {
                        metaBuilder.frameSrc(m.getFrameSrc());
                    }
                    if (m.getAuths() != null && !m.getAuths().isEmpty()) {
                        metaBuilder.auths(Arrays.asList(m.getAuths().split(",")));
                    }
                    builder.meta(metaBuilder.build());

                    // 递归构建子路由
                    List<AsyncRouteVO> children = buildRouteTree(menus, m.getId());
                    if (!children.isEmpty()) {
                        builder.children(children);
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());
    }
}
