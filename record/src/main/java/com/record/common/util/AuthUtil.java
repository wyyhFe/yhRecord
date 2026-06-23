package com.record.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 权限工具类。
 * <p>提供当前登录用户的角色判断，供业务层做 admin 全量数据查询 bypass。</p>
 */
public final class AuthUtil {

    private static final String ROLE_ADMIN = "ROLE_admin";

    /**
     * 当前请求是否为管理员。
     * <p>在 JwtAuthenticationFilter 已验证通过的前提下，从 SecurityContext 读取 authorities。</p>
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }

    private AuthUtil() {
    }
}
