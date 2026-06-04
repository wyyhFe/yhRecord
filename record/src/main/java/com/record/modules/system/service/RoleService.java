package com.record.modules.system.service;

import com.record.modules.system.model.entity.Role;
import com.record.modules.system.model.entity.UserRole;

import java.util.List;

/**
 * 角色服务。
 */
public interface RoleService {

    /**
     * 获取用户角色列表。
     */
    List<String> getRoleNamesByUserId(Long userId);

    /**
     * 获取用户角色关联记录。
     */
    List<UserRole> getUserRoles(Long userId);

    /**
     * 获取所有角色。
     */
    List<Role> listAll();

    /**
     * 保存或更新角色。
     */
    Role saveOrUpdateRole(Role role);

    /**
     * 分配角色菜单权限。
     */
    void assignMenus(Long roleId, List<Long> menuIds);
}
