-- 管理后台登录功能：sys_user 增加 username + password_hash + display_name
-- 适用数据库：MySQL 8.x

ALTER TABLE `sys_user`
    ADD COLUMN `username` VARCHAR(50)  DEFAULT NULL COMMENT '管理后台登录用户名',
    ADD COLUMN `password` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希（BCrypt）',
    ADD UNIQUE KEY `uk_username` (`username`);

-- 新增 ADMIN 登录来源：旧约束存在则删除，然后重建
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND CONSTRAINT_NAME = 'chk_user_login_type') > 0,
    'ALTER TABLE `sys_user` DROP CHECK `chk_user_login_type`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `sys_user` ADD CONSTRAINT `chk_user_login_type`
    CHECK (`login_type` IN ('WECHAT', 'GITHUB', 'GOOGLE', 'ADMIN'));
