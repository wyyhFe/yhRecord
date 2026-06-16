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
        // 获取用户的角色列表
        List<String> userRoles = roleService.getRoleNamesByUserId(userId);

        // 查询所有启用的非按钮类型菜单，按 id 排序
        LambdaQueryWrapper<Menu> query = new LambdaQueryWrapper<Menu>()
                .eq(Menu::getStatus, CommonStatus.ENABLED)
                .ne(Menu::getMenuType, "BUTTON")
                .orderByAsc(Menu::getId);
        List<Menu> menus = menuMapper.selectList(query);

        // 根据 meta.roles 字段过滤菜单
        List<Menu> filteredMenus = menus.stream()
                .filter(menu -> {
                    Map<String, Object> meta = menu.getMeta();
                    if (meta == null) {
                        return true;
                    }
                    Object rolesObj = meta.get("roles");
                    // 如果没有设置 roles，则所有角色都可以访问
                    if (rolesObj == null) {
                        return true;
                    }
                    // 检查用户是否有任何一个角色
                    List<String> menuRoles;
                    if (rolesObj instanceof List) {
                        menuRoles = (List<String>) rolesObj;
                    } else {
                        menuRoles = Arrays.asList(rolesObj.toString().split(","));
                    }
                    return menuRoles.stream().anyMatch(userRoles::contains);
                })
                .collect(Collectors.toList());

        return buildRouteTree(filteredMenus, null);
    }

    @Override
    public List<Menu> listAllEnabled() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getStatus, CommonStatus.ENABLED)
                        .orderByAsc(Menu::getId));
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

    // ============ 辅助方法，安全地从 Map 中获取值 ============

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Integer) {
            return ((Integer) value) == 1;
        }
        return null;
    }

    private List<String> getStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.startsWith("[")) {
                // JSON 数组格式
                try {
                    return new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(str, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                } catch (Exception e) {
                    return Arrays.asList(str.split(","));
                }
            }
            return Arrays.asList(str.split(","));
        }
        return null;
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

                    // 构建 meta（从 JSON 字段中读取）
                    Map<String, Object> metaMap = m.getMeta();
                    AsyncRouteVO.MetaVO.MetaVOBuilder metaBuilder = AsyncRouteVO.MetaVO.builder();

                    if (metaMap != null) {
                        metaBuilder.title(getString(metaMap, "title"));
                        metaBuilder.icon(getString(metaMap, "icon"));
                        metaBuilder.rank(getInteger(metaMap, "rank"));
                        metaBuilder.showLink(getBoolean(metaMap, "showLink"));
                        metaBuilder.showParent(getBoolean(metaMap, "showParent"));
                        metaBuilder.keepAlive(getBoolean(metaMap, "keepAlive"));
                        metaBuilder.frameSrc(getString(metaMap, "frameSrc"));
                        metaBuilder.frameLoading(getBoolean(metaMap, "frameLoading"));
                        metaBuilder.hiddenTag(getBoolean(metaMap, "hiddenTag"));
                        metaBuilder.activePath(getString(metaMap, "activePath"));
                        metaBuilder.auths(getStringList(metaMap, "auths"));
                        metaBuilder.roles(getStringList(metaMap, "roles"));
                    }

                    // 如果没有 title，使用数据库的 title 字段
                    AsyncRouteVO.MetaVO meta = metaBuilder.build();
                    if (meta.getTitle() == null && m.getTitle() != null) {
                        metaBuilder.title(m.getTitle());
                        meta = metaBuilder.build();
                    }

                    builder.meta(meta);

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
