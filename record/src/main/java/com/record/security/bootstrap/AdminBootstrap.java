package com.record.security.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.config.AppProperties;
import com.record.modules.system.mapper.RoleMapper;
import com.record.modules.system.mapper.UserRoleMapper;
import com.record.modules.system.model.entity.Role;
import com.record.modules.system.model.entity.UserRole;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 启动期自动给配置里指定的 userId 赋 admin 角色。
 * <p>解决"系统第一个 admin 怎么诞生"的鸡生蛋问题：先在 application-dev.yaml 写自己的 userId，
 * 启动后自动挂上 admin。已经是 admin 的不会重复 insert，找不到的用户会跳过并打日志。</p>
 */
@Configuration
public class AdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final String ADMIN_ROLE_NAME = "admin";

    @Bean
    public ApplicationRunner adminBootstrapRunner(AppProperties appProperties,
                                                   UserMapper userMapper,
                                                   RoleMapper roleMapper,
                                                   UserRoleMapper userRoleMapper) {
        return args -> bootstrap(appProperties, userMapper, roleMapper, userRoleMapper);
    }

    @Transactional
    public void bootstrap(AppProperties appProperties,
                          UserMapper userMapper,
                          RoleMapper roleMapper,
                          UserRoleMapper userRoleMapper) {
        List<Long> targetUserIds = appProperties.getSecurity().getBootstrapAdminUserIds();
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            log.info("[admin-bootstrap] 未配置 app.security.bootstrap-admin-user-ids，跳过");
            return;
        }

        Role adminRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getName, ADMIN_ROLE_NAME)
                .last("LIMIT 1"));
        if (adminRole == null) {
            log.warn("[admin-bootstrap] sys_role 表里没有 name=admin 的角色，请先确认 V20260604_2 迁移已执行");
            return;
        }

        for (Long userId : targetUserIds) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("[admin-bootstrap] userId={} 在 sys_user 中不存在，跳过", userId);
                continue;
            }
            Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId)
                    .eq(UserRole::getRoleId, adminRole.getId()));
            if (count != null && count > 0) {
                log.info("[admin-bootstrap] userId={} 已是 admin，跳过", userId);
                continue;
            }
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(adminRole.getId());
            userRoleMapper.insert(ur);
            log.info("[admin-bootstrap] 已为 userId={} 赋予 admin 角色", userId);
        }
    }
}
