package com.record.modules.system.service;

/**
 * 后台账号合并：把 source 用户的所有业务数据与绑定关系迁移到 target，并禁用 source。
 */
public interface UserMergeService {

    /**
     * 合并用户。
     * <ul>
     *     <li>迁移 sys_user_identity、sys_user_role 到 target，去重</li>
     *     <li>17 张业务表中 source 的数据全部改写 user_id 为 target</li>
     *     <li>sys_user_session source 端直接清空，强制下线</li>
     *     <li>source 的 openid 若非空且 target 没有，自动接管</li>
     *     <li>source 状态置为 DISABLED 并写入 merged_into_user_id 形成审计</li>
     * </ul>
     */
    void merge(Long sourceUserId, Long targetUserId);
}
