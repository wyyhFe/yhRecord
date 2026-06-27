package com.record.modules.system.service;

import com.record.modules.system.model.entity.Menu;
import com.record.modules.system.model.vo.AsyncRouteVO;

import java.util.List;

/**
 * 菜单服务。
 */
public interface MenuService {

    /**
     * 根据用户 ID 和平台获取动态路由（PureAdmin 格式）。
     * userId 为 null 时仅返回无角色限制的公开菜单。
     * platform 为 null 时不过滤平台（返回全部）。
     */
    List<AsyncRouteVO> getAsyncRoutesByUserId(Long userId, String platform);

    /**
     * 获取所有启用的菜单列表（扁平）。
     */
    List<Menu> listAllEnabled();

    /**
     * 创建或更新菜单。
     */
    Menu saveOrUpdateMenu(Menu menu);

    /**
     * 删除菜单。
     */
    void deleteMenu(Long menuId);
}
