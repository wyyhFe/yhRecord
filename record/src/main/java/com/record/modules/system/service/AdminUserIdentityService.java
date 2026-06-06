package com.record.modules.system.service;

import com.record.modules.user.model.vo.UserIdentityVO;

import java.util.List;

/**
 * 后台第三方账号绑定管理。
 * 区别于用户端：
 * <ul>
 *     <li>不做"至少保留一个"校验，允许强制解绑</li>
 *     <li>按 identity 主键 ID 操作，避免误删错平台</li>
 * </ul>
 */
public interface AdminUserIdentityService {

    List<UserIdentityVO> listByUserId(Long userId);

    /** 强制解绑：按主键删除；微信平台会同时清掉 sys_user.openid。 */
    void forceUnbind(Long userId, Long identityId);
}
