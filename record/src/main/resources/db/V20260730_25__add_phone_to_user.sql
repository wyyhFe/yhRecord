ALTER TABLE `sys_user`
    ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（绑定后可用于管理后台登录）',
    ADD UNIQUE KEY `uk_phone` (`phone`);
