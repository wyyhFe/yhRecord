package com.record.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.modules.system.mapper.MenuMapper;
import com.record.modules.system.model.entity.Menu;
import com.record.modules.system.model.vo.AsyncRouteVO;
import com.record.modules.system.service.MenuService;
import com.record.modules.system.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现。
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final String ADMIN_ROLE = "admin";

    private final MenuMapper menuMapper;
    private final RoleService roleService;

    public MenuServiceImpl(MenuMapper menuMapper, RoleService roleService) {
        this.menuMapper = menuMapper;
        this.roleService = roleService;
    }

    @Override
    public List<AsyncRouteVO> getAsyncRoutesByUserId(Long userId) {
        // 简化逻辑：只区分 admin / 非 admin
        //   admin     → 所有启用菜单
        //   非 admin  → 所有启用菜单中 admin_only != true 的部分
        // 不再走 sys_role_menu，那张关联表保留给将来更细粒度的权限场景
        boolean isAdmin = roleService.getRoleNamesByUserId(userId).contains(ADMIN_ROLE);

        LambdaQueryWrapper<Menu> query = new LambdaQueryWrapper<Menu>()
                .eq(Menu::getStatus, CommonStatus.ENABLED)
                .ne(Menu::getMenuType, "BUTTON")
                .orderByAsc(Menu::getRank);
        if (!isAdmin) {
            // adminOnly 默认 0；用 ne(true) 同时覆盖 false 和 NULL 两种历史数据
            query.and(w -> w.ne(Menu::getAdminOnly, true).or().isNull(Menu::getAdminOnly));
        }
        List<Menu> menus = menuMapper.selectList(query);

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
