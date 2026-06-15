CREATE TABLE IF NOT EXISTS `biz_checkin_media` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `record_id` BIGINT NOT NULL COMMENT '打卡记录 ID',
  `media_type` VARCHAR(16) NOT NULL COMMENT '附件类型：IMAGE',
  `file_path` VARCHAR(255) NOT NULL COMMENT '文件相对路径',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户 ID，系统任务写 0',
  `updated_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人用户 ID，系统任务写 0',
  PRIMARY KEY (`id`),
  KEY `idx_checkin_media_record` (`record_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡附件表';
