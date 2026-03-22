-- 生活记录项目初始化建表脚本
-- 适用数据库：MySQL 8.x

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL COMMENT '用户 ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '小程序 openid',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `avatar_path` VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
  `gender` VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN' COMMENT '性别：UNKNOWN / MALE / FEMALE',
  `official_account_openid` VARCHAR(64) DEFAULT NULL COMMENT '公众号 openid',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `signature` VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED / DISABLED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_openid` (`openid`),
  CONSTRAINT `chk_user_gender` CHECK (`gender` IN ('UNKNOWN', 'MALE', 'FEMALE')),
  CONSTRAINT `chk_user_status` CHECK (`status` IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `user_session` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '当前会话 ID',
  `refresh_token` VARCHAR(512) NOT NULL COMMENT '刷新令牌',
  `refresh_expire_at` DATETIME NOT NULL COMMENT '刷新令牌过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_session_user` (`user_id`),
  UNIQUE KEY `uk_user_session_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录会话表';

CREATE TABLE IF NOT EXISTS `tag_template` (
  `id` BIGINT NOT NULL COMMENT '模板 ID',
  `name` VARCHAR(64) NOT NULL COMMENT '模板名称',
  `color` VARCHAR(32) DEFAULT NULL COMMENT '模板颜色',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '模板图标',
  `module_type` VARCHAR(16) NOT NULL COMMENT '所属模块：DIARY / LEDGER',
  `ledger_type` VARCHAR(16) DEFAULT NULL COMMENT '记账标签类型：EXPENSE / INCOME，仅 LEDGER 模块使用',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED / DISABLED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_tag_template_module_status` (`module_type`, `status`, `sort_order`),
  CONSTRAINT `chk_tag_template_module_type` CHECK (`module_type` IN ('DIARY', 'LEDGER')),
  CONSTRAINT `chk_tag_template_ledger_type` CHECK (`ledger_type` IS NULL OR `ledger_type` IN ('EXPENSE', 'INCOME')),
  CONSTRAINT `chk_tag_template_status` CHECK (`status` IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统标签模板表';

CREATE TABLE IF NOT EXISTS `user_tag` (
  `id` BIGINT NOT NULL COMMENT '标签 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `template_id` BIGINT DEFAULT NULL COMMENT '来源模板 ID',
  `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
  `color` VARCHAR(32) DEFAULT NULL COMMENT '标签颜色',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '标签图标',
  `module_type` VARCHAR(16) NOT NULL COMMENT '所属模块：DIARY / LEDGER',
  `ledger_type` VARCHAR(16) DEFAULT NULL COMMENT '记账标签类型：EXPENSE / INCOME，仅 LEDGER 模块使用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag_user_module_ledger_name` (`user_id`, `module_type`, `ledger_type`, `name`),
  KEY `idx_user_tag_template` (`template_id`),
  CONSTRAINT `chk_user_tag_module_type` CHECK (`module_type` IN ('DIARY', 'LEDGER')),
  CONSTRAINT `chk_user_tag_ledger_type` CHECK (`ledger_type` IS NULL OR `ledger_type` IN ('EXPENSE', 'INCOME'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户标签表';

CREATE TABLE IF NOT EXISTS `diary` (
  `id` BIGINT NOT NULL COMMENT '日记 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '正文内容',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `weather` VARCHAR(32) DEFAULT NULL COMMENT '天气',
  `mood` VARCHAR(32) DEFAULT NULL COMMENT '心情',
  `visibility` VARCHAR(16) NOT NULL DEFAULT 'PRIVATE' COMMENT '可见范围：PRIVATE / SHARED / PUBLIC',
  `location_name` VARCHAR(128) DEFAULT NULL COMMENT '地点名称',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '完整地址',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(64) DEFAULT NULL COMMENT '区县',
  `latitude` DOUBLE DEFAULT NULL COMMENT '纬度',
  `longitude` DOUBLE DEFAULT NULL COMMENT '经度',
  `location_source_type` VARCHAR(16) DEFAULT NULL COMMENT '定位来源：CURRENT / MANUAL',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_diary_user_record_date` (`user_id`, `record_date`),
  KEY `idx_diary_user_deleted_at` (`user_id`, `deleted_at`),
  CONSTRAINT `chk_diary_visibility` CHECK (`visibility` IN ('PRIVATE', 'SHARED', 'PUBLIC')),
  CONSTRAINT `chk_diary_location_source` CHECK (`location_source_type` IS NULL OR `location_source_type` IN ('CURRENT', 'MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记表';

CREATE TABLE IF NOT EXISTS `diary_media` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `diary_id` BIGINT NOT NULL COMMENT '日记 ID',
  `media_type` VARCHAR(16) NOT NULL COMMENT '附件类型：IMAGE / VIDEO / AUDIO',
  `file_path` VARCHAR(255) NOT NULL COMMENT '文件相对路径',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_diary_media_diary` (`diary_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记附件表';

CREATE TABLE IF NOT EXISTS `diary_tag_rel` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `diary_id` BIGINT NOT NULL COMMENT '日记 ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_diary_tag_rel` (`diary_id`, `tag_id`),
  KEY `idx_diary_tag_rel_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记与标签关联表';

CREATE TABLE IF NOT EXISTS `diary_comment` (
  `id` BIGINT NOT NULL COMMENT '评论 ID',
  `diary_id` BIGINT NOT NULL COMMENT '日记 ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户 ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论 ID，顶级评论为空',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_diary_comment_diary` (`diary_id`, `created_at`),
  KEY `idx_diary_comment_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记评论表';

CREATE TABLE IF NOT EXISTS `diary_like` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `diary_id` BIGINT NOT NULL COMMENT '日记 ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_diary_like_diary_user` (`diary_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记点赞表';

CREATE TABLE IF NOT EXISTS `ledger_book` (
  `id` BIGINT NOT NULL COMMENT '账本 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `name` VARCHAR(64) NOT NULL COMMENT '账本名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '账本描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_ledger_book_user` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本表';

CREATE TABLE IF NOT EXISTS `ledger_entry` (
  `id` BIGINT NOT NULL COMMENT '流水 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `book_id` BIGINT NOT NULL COMMENT '账本 ID',
  `type` VARCHAR(16) NOT NULL COMMENT '流水类型：INCOME / EXPENSE',
  `amount` DECIMAL(12, 2) NOT NULL COMMENT '金额，必须大于 0',
  `entry_date` DATE NOT NULL COMMENT '记账日期',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `image_path` VARCHAR(255) DEFAULT NULL COMMENT '图片路径',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_ledger_entry_user_date` (`user_id`, `entry_date`),
  KEY `idx_ledger_entry_book_date` (`book_id`, `entry_date`),
  CONSTRAINT `chk_ledger_entry_type` CHECK (`type` IN ('INCOME', 'EXPENSE')),
  CONSTRAINT `chk_ledger_entry_amount` CHECK (`amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记账流水表';

CREATE TABLE IF NOT EXISTS `ledger_entry_tag_rel` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `entry_id` BIGINT NOT NULL COMMENT '流水 ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ledger_entry_tag_rel` (`entry_id`, `tag_id`),
  KEY `idx_ledger_entry_tag_rel_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水与标签关联表';

CREATE TABLE IF NOT EXISTS `checkin_task` (
  `id` BIGINT NOT NULL COMMENT '任务 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `name` VARCHAR(128) NOT NULL COMMENT '任务名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '任务描述',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED / DISABLED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_checkin_task_user_status` (`user_id`, `status`),
  CONSTRAINT `chk_checkin_task_status` CHECK (`status` IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡任务表';

CREATE TABLE IF NOT EXISTS `checkin_record` (
  `id` BIGINT NOT NULL COMMENT '打卡记录 ID',
  `task_id` BIGINT NOT NULL COMMENT '任务 ID',
  `user_id` BIGINT NOT NULL COMMENT '打卡用户 ID',
  `checkin_date` DATE NOT NULL COMMENT '打卡业务日期',
  `checked_at` DATETIME NOT NULL COMMENT '实际打卡时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '打卡备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkin_record_task_user_date` (`task_id`, `user_id`, `checkin_date`),
  KEY `idx_checkin_record_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录表';

CREATE TABLE IF NOT EXISTS `memorial_day` (
  `id` BIGINT NOT NULL COMMENT '纪念日 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `type` VARCHAR(32) DEFAULT NULL COMMENT '纪念日类型',
  `memorial_date` DATE NOT NULL COMMENT '纪念日期',
  `annual_repeat` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否每年重复',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `remind_at` DATETIME DEFAULT NULL COMMENT '提醒时间',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED / DISABLED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_memorial_day_user_date` (`user_id`, `memorial_date`),
  CONSTRAINT `chk_memorial_day_status` CHECK (`status` IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纪念日表';

CREATE TABLE IF NOT EXISTS `recycle_bin` (
  `id` BIGINT NOT NULL COMMENT '回收站记录 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '资源类型：DIARY / LEDGER_ENTRY / CHECKIN_TASK / MEMORIAL_DAY',
  `resource_id` BIGINT NOT NULL COMMENT '资源 ID',
  `deleted_at` DATETIME NOT NULL COMMENT '删除时间',
  `expire_at` DATETIME NOT NULL COMMENT '过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_recycle_bin_user_expire` (`user_id`, `expire_at`),
  CONSTRAINT `chk_recycle_bin_resource_type` CHECK (`resource_type` IN ('DIARY', 'LEDGER_ENTRY', 'CHECKIN_TASK', 'MEMORIAL_DAY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回收站表';

CREATE TABLE IF NOT EXISTS `reminder_setting` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `diary_reminder_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用日记提醒',
  `mini_program_reminder_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用小程序订阅消息通道',
  `official_account_reminder_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用公众号模板消息通道',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reminder_setting_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒设置表';

CREATE TABLE IF NOT EXISTS `reminder_log` (
  `id` BIGINT NOT NULL COMMENT '日志 ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
  `business_type` VARCHAR(32) NOT NULL COMMENT '业务类型：DIARY_DAILY / LEDGER_DAILY / LEDGER_MONTHLY / MEMORIAL_DAY',
  `channel` VARCHAR(32) NOT NULL COMMENT '发送通道：MINI_PROGRAM / OFFICIAL_ACCOUNT',
  `target_id` BIGINT DEFAULT NULL COMMENT '目标业务 ID',
  `business_date` DATE NOT NULL COMMENT '业务日期',
  `send_status` VARCHAR(16) NOT NULL COMMENT '发送状态：SUCCESS / FAILED',
  `send_message` VARCHAR(255) DEFAULT NULL COMMENT '发送结果说明',
  `sent_at` DATETIME DEFAULT NULL COMMENT '发送时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_reminder_log_user_date` (`user_id`, `business_date`),
  KEY `idx_reminder_log_business_channel` (`business_type`, `channel`, `business_date`),
  CONSTRAINT `chk_reminder_log_business_type` CHECK (`business_type` IN ('DIARY_DAILY', 'LEDGER_DAILY', 'LEDGER_MONTHLY', 'MEMORIAL_DAY')),
  CONSTRAINT `chk_reminder_log_channel` CHECK (`channel` IN ('MINI_PROGRAM', 'OFFICIAL_ACCOUNT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒发送日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- 旧库升级参考 SQL
-- 日记模块已移除单篇提醒字段，仅保留全局日记提醒开关，固定每天 22:00 发送提醒。
-- 如果你的数据库还是旧表结构，请执行下面这条语句删除 diary.remind_at。
--
-- ALTER TABLE diary DROP COLUMN remind_at;

-- 旧库升级参考 SQL
-- 记账标签现已支持区分支出标签和收入标签。
-- 需要在标签模板表和用户标签表中补充 ledger_type 字段。
-- 唯一索引也要从“用户 + 模块 + 名称”调整为“用户 + 模块 + 记账标签类型 + 名称”。
--
-- ALTER TABLE tag_template
--   ADD COLUMN ledger_type VARCHAR(16) NULL COMMENT '记账标签类型：EXPENSE / INCOME' AFTER module_type;
--
-- ALTER TABLE user_tag
--   ADD COLUMN ledger_type VARCHAR(16) NULL COMMENT '记账标签类型：EXPENSE / INCOME' AFTER module_type;
--
-- ALTER TABLE tag_template
--   ADD CONSTRAINT chk_tag_template_ledger_type CHECK (ledger_type IS NULL OR ledger_type IN ('EXPENSE', 'INCOME'));
--
-- ALTER TABLE user_tag
--   ADD CONSTRAINT chk_user_tag_ledger_type CHECK (ledger_type IS NULL OR ledger_type IN ('EXPENSE', 'INCOME'));
--
-- DROP INDEX uk_user_tag_user_module_name ON user_tag;
-- CREATE UNIQUE INDEX uk_user_tag_user_module_ledger_name ON user_tag (user_id, module_type, ledger_type, name);
