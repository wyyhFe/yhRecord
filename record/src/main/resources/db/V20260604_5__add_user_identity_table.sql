-- 引入第三方账号绑定关系：sys_user_identity
-- 一个用户可以关联多条 identity，每条 identity 对应一个第三方平台账号
-- 同一个 (provider, provider_user_id) 全局唯一，防止一个第三方账号被多个本系统用户绑定

USE life_record;

CREATE TABLE IF NOT EXISTS `sys_user_identity` (
  `id`               BIGINT       NOT NULL COMMENT '主键 ID',
  `user_id`          BIGINT       NOT NULL COMMENT '关联 sys_user.id',
  `provider`         VARCHAR(32)  NOT NULL COMMENT '第三方平台：WECHAT / GITHUB / GOOGLE',
  `provider_user_id` VARCHAR(128) NOT NULL COMMENT '第三方平台用户唯一 ID',
  `nickname`         VARCHAR(64)  DEFAULT NULL COMMENT '绑定时第三方平台昵称快照',
  `avatar_url`       VARCHAR(512) DEFAULT NULL COMMENT '绑定时第三方平台头像快照',
  `bound_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`       BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by`       BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_user` (`provider`, `provider_user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户第三方平台绑定关系';

-- 开发态：不做数据迁移，直接清掉 sys_user 上不再作为登录入口的冗余字段
-- openid 保留：仍用于公众号/订阅消息推送
DELIMITER //

DROP PROCEDURE IF EXISTS _drop_column_if_exists//
DROP PROCEDURE IF EXISTS _drop_index_if_exists//

CREATE PROCEDURE _drop_column_if_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64)
)
BEGIN
    DECLARE col_count INT;
    SELECT COUNT(*) INTO col_count
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl_name AND COLUMN_NAME = col_name;

    IF col_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tbl_name, '` DROP COLUMN `', col_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

CREATE PROCEDURE _drop_index_if_exists(
    IN tbl_name VARCHAR(64),
    IN idx_name VARCHAR(64)
)
BEGIN
    DECLARE idx_count INT;
    SELECT COUNT(*) INTO idx_count
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl_name AND INDEX_NAME = idx_name;

    IF idx_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tbl_name, '` DROP INDEX `', idx_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DELIMITER ;

-- 先删依赖在列上的索引，再删列
CALL _drop_index_if_exists('sys_user', 'idx_user_github_id');
CALL _drop_index_if_exists('sys_user', 'idx_user_google_id');

CALL _drop_column_if_exists('sys_user', 'github_id');
CALL _drop_column_if_exists('sys_user', 'google_id');

DROP PROCEDURE IF EXISTS _drop_column_if_exists;
DROP PROCEDURE IF EXISTS _drop_index_if_exists;
