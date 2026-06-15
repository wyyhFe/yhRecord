-- 勋章定义表
CREATE TABLE IF NOT EXISTS `biz_achievement_medal` (
  `id` BIGINT NOT NULL COMMENT '勋章 ID',
  `code` VARCHAR(64) NOT NULL COMMENT '勋章唯一编码',
  `name` VARCHAR(64) NOT NULL COMMENT '勋章名称',
  `description` VARCHAR(255) NOT NULL COMMENT '解锁条件描述',
  `icon` VARCHAR(8) NOT NULL COMMENT '勋章图标 Emoji',
  `category` VARCHAR(32) NOT NULL COMMENT '分类：STREAK / TOTAL / TIME / SPECIAL',
  `difficulty` INT NOT NULL DEFAULT 1 COMMENT '难度等级 1-6',
  `condition_value` INT NOT NULL DEFAULT 0 COMMENT '解锁阈值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medal_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='勋章定义表';

-- 用户勋章解锁记录
CREATE TABLE IF NOT EXISTS `biz_user_medal` (
  `id` BIGINT NOT NULL COMMENT '记录 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `medal_id` BIGINT NOT NULL COMMENT '勋章 ID',
  `unlocked_at` DATETIME NOT NULL COMMENT '解锁时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_medal` (`user_id`, `medal_id`),
  KEY `idx_user_medal_user` (`user_id`, `unlocked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户勋章解锁记录';

-- 打卡记录增加补卡标记
ALTER TABLE `biz_checkin_record` ADD COLUMN `is_mend` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否补卡：0-正常打卡 1-补卡' AFTER `mood`;

-- 初始化勋章数据
INSERT INTO `biz_achievement_medal` (`id`, `code`, `name`, `description`, `icon`, `category`, `difficulty`, `condition_value`, `sort_order`) VALUES
(1, 'streak_3', '初心者', '连续打卡 3 天', '🔥', 'STREAK', 1, 3, 1),
(2, 'streak_7', '小坚持', '连续打卡 7 天', '🔥', 'STREAK', 2, 7, 2),
(3, 'streak_21', '习惯养成', '连续打卡 21 天', '🔥', 'STREAK', 3, 21, 3),
(4, 'streak_30', '自律达人', '连续打卡 30 天', '🔥', 'STREAK', 4, 30, 4),
(5, 'streak_100', '钢铁意志', '连续打卡 100 天', '🔥', 'STREAK', 5, 100, 5),
(6, 'total_100', '百次打卡', '累计打卡 100 次', '📊', 'TOTAL', 3, 100, 6),
(7, 'total_1000', '千次打卡', '累计打卡 1000 次', '📊', 'TOTAL', 5, 1000, 7),
(8, 'early_bird_7', '早起鸟', '6:00 前打卡累计 7 次', '⏰', 'TIME', 3, 7, 8),
(9, 'night_owl_7', '夜猫子', '23:00 后打卡累计 7 次', '⏰', 'TIME', 2, 7, 9),
(10, 'all_done_day', '全勤日', '一天内完成所有任务', '⭐', 'SPECIAL', 2, 1, 10),
(11, 'multi_task_5', '多面手', '同时拥有 5 个以上任务', '⭐', 'SPECIAL', 2, 5, 11),
(12, 'comeback', '回归者', '断签后重新连续 7 天', '⭐', 'SPECIAL', 3, 7, 12);
