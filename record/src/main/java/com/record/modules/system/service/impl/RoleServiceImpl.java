package com.record.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.modules.system.mapper.RoleMapper;
import com.record.modules.system.mapper.RoleMenuMapper;
import com.record.modules.system.mapper.UserRoleMapper;
import com.record.modules.system.model.entity.Role;
import com.record.modules.system.model.entity.RoleMenu;
import com.record.modules.system.model.entity.UserRole;
import com.record.modules.system.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现。
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;

    public RoleServiceImpl(RoleMapper roleMapper, UserRoleMapper userRoleMapper, RoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }

    @Override
    public List<UserRole> getUserRoles(Long userId) {
        return userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    @Override
    public List<Role> listAll() {
        return roleMapper.selectList(null);
    }

    @Override
    public Role saveOrUpdateRole(Role role) {
        roleMapper.insertOrUpdate(role);
        return role;
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除旧关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        // 插入新关联
        for (Long menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }
}
