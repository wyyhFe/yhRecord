-- 打卡标签定义表
CREATE TABLE IF NOT EXISTS `biz_checkin_tag` (
  `id` BIGINT NOT NULL COMMENT '标签 ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户 ID（系统预设为 NULL）',
  `name` VARCHAR(32) NOT NULL COMMENT '标签名称',
  `icon` VARCHAR(8) DEFAULT NULL COMMENT '标签图标 Emoji',
  `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统预设',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tag_user` (`user_id`),
  KEY `idx_tag_system` (`is_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡标签定义表';

-- 打卡记录与标签关联表
CREATE TABLE IF NOT EXISTS `biz_checkin_record_tag` (
  `id` BIGINT NOT NULL COMMENT '记录 ID',
  `record_id` BIGINT NOT NULL COMMENT '打卡记录 ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_tag` (`record_id`, `tag_id`),
  KEY `idx_record_tag_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录与标签关联表';

-- 打卡记录增加心情字段
ALTER TABLE `biz_checkin_record` ADD COLUMN `mood` VARCHAR(8) DEFAULT NULL COMMENT '打卡心情 Emoji' AFTER `remark`;

-- 初始化系统预设标签
INSERT INTO `biz_checkin_tag` (`id`, `user_id`, `name`, `icon`, `is_system`) VALUES
(1, NULL, '运动出汗', '💪', 1),
(2, NULL, '跑步', '🏃', 1),
(3, NULL, '健身', '🏋️', 1),
(4, NULL, '瑜伽', '🧘', 1),
(5, NULL, '早起成功', '🌅', 1),
(6, NULL, '晨型人', '☀️', 1),
(7, NULL, '健康饮食', '🥗', 1),
(8, NULL, '没喝奶茶', '🧋', 1),
(9, NULL, '自己做饭', '🍳', 1),
(10, NULL, '多喝水', '💧', 1),
(11, NULL, '读书', '📖', 1),
(12, NULL, '背单词', '📝', 1),
(13, NULL, '练琴', '🎹', 1),
(14, NULL, '学习打卡', '📚', 1),
(15, NULL, '早睡成功', '🌙', 1),
(16, NULL, '充足睡眠', '😴', 1),
(17, NULL, '整理房间', '🧹', 1),
(18, NULL, '冥想', '🧠', 1),
(19, NULL, '不刷手机', '📵', 1),
(20, NULL, '断舍离', '✨', 1);
