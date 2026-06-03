-- OAuth 登录字段扩展
-- 执行前请备份数据库

ALTER TABLE `user`
    ADD COLUMN `github_id` VARCHAR(64) DEFAULT NULL COMMENT 'GitHub 用户唯一 ID' AFTER `openid`,
    ADD COLUMN `google_id` VARCHAR(128) DEFAULT NULL COMMENT 'Google 用户唯一 ID（sub claim）' AFTER `github_id`,
    ADD COLUMN `login_type` VARCHAR(20) DEFAULT 'WECHAT' COMMENT '注册来源：WECHAT / GITHUB / GOOGLE' AFTER `google_id`;

-- 索引：按 GitHub ID 查找用户
CREATE INDEX `idx_user_github_id` ON `user` (`github_id`);

-- 索引：按 Google ID 查找用户
CREATE INDEX `idx_user_google_id` ON `user` (`google_id`);
