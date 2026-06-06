package com.record.modules.system.service.impl;

import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.modules.system.service.UserMergeService;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台账号合并实现。
 * 用 JdbcTemplate 跑原生 SQL，避免 MyBatis-Plus 单表 update 在跨多张表场景下的样板代码。
 */
@Service
public class UserMergeServiceImpl implements UserMergeService {

    private static final Logger log = LoggerFactory.getLogger(UserMergeServiceImpl.class);

    /** 业务表里 user_id 唯一的（直接 UPDATE 不会撞唯一索引）。 */
    private static final List<String> SIMPLE_TABLES = List.of(
            "biz_diary",
            "biz_diary_comment",
            "biz_ledger_book",
            "biz_ledger_entry",
            "biz_checkin_task",
            "biz_checkin_record",
            "biz_memorial_day",
            "biz_recycle_bin",
            "biz_reminder_log",
            "biz_ai_bill_analysis_record",
            "biz_ai_call_log",
            "biz_knowledge_base",
            "biz_knowledge_document"
    );

    /**
     * 业务/系统表里 user_id 参与 UNIQUE 索引的；
     * 用 UPDATE IGNORE 把 source 的搬到 target，撞 UK 的留着，再 DELETE 清掉剩余 source。
     * 含义：target 已有相同 key 时优先保留 target 的，丢掉 source 的。
     */
    private static final List<String> UK_CONFLICT_TABLES = List.of(
            "biz_user_tag",         // (user_id, module_type, ledger_type, name) UK
            "biz_diary_like",       // (diary_id, user_id) UK
            "biz_reminder_setting", // (user_id) UK
            "sys_user_role",        // (user_id, role_id) UK
            "sys_user_identity"     // (provider, provider_user_id) UK
    );

    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public UserMergeServiceImpl(UserMapper userMapper,
                                JdbcTemplate jdbcTemplate,
                                StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    @Transactional
    public void merge(Long sourceUserId, Long targetUserId) {
        if (sourceUserId == null || targetUserId == null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "source/target 用户 ID 必填");
        }
        if (sourceUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.USER_ERROR, "源用户和目标用户不能相同");
        }
        User source = userMapper.selectById(sourceUserId);
        User target = userMapper.selectById(targetUserId);
        if (source == null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "源用户不存在");
        }
        if (target == null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "目标用户不存在");
        }
        if (source.getMergedIntoUserId() != null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "源用户已被合并过，无法重复合并");
        }
        if (target.getMergedIntoUserId() != null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "目标用户已被合并到其他账号，无法作为合并目标");
        }

        log.info("[merge] 开始合并 source={} → target={}", sourceUserId, targetUserId);

        for (String table : SIMPLE_TABLES) {
            int n = jdbcTemplate.update(
                    "UPDATE `" + table + "` SET user_id = ? WHERE user_id = ?",
                    targetUserId, sourceUserId);
            if (n > 0) log.info("[merge] {} 移动 {} 行", table, n);
        }

        for (String table : UK_CONFLICT_TABLES) {
            int moved = jdbcTemplate.update(
                    "UPDATE IGNORE `" + table + "` SET user_id = ? WHERE user_id = ?",
                    targetUserId, sourceUserId);
            int dropped = jdbcTemplate.update(
                    "DELETE FROM `" + table + "` WHERE user_id = ?",
                    sourceUserId);
            if (moved + dropped > 0) {
                log.info("[merge] {} 移动 {} 行，丢弃重复 {} 行", table, moved, dropped);
            }
        }

        // session 不合并，强制下线 source（会话信息存在 Redis 里）
        Boolean dropped = stringRedisTemplate.delete(RedisKeyConstants.USER_SESSION + sourceUserId);
        if (Boolean.TRUE.equals(dropped)) {
            log.info("[merge] Redis 中 source 用户会话已清除（强制下线）");
        }

        // openid 接管：target 没有就把 source 的搬过去（先清 source 释放 UK）
        String srcOpenid = source.getOpenid();
        String tgtOpenid = target.getOpenid();
        if (srcOpenid != null && !srcOpenid.isBlank() && (tgtOpenid == null || tgtOpenid.isBlank())) {
            jdbcTemplate.update("UPDATE `sys_user` SET openid = NULL WHERE id = ?", sourceUserId);
            jdbcTemplate.update("UPDATE `sys_user` SET openid = ? WHERE id = ?", srcOpenid, targetUserId);
            log.info("[merge] openid {} 转移到 target", srcOpenid);
        }

        // source 标记为已合并 + 禁用
        jdbcTemplate.update(
                "UPDATE `sys_user` SET status = 'DISABLED', merged_into_user_id = ? WHERE id = ?",
                targetUserId, sourceUserId);

        log.info("[merge] 合并完成 source={} → target={}", sourceUserId, targetUserId);
    }
}
