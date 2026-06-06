-- 给菜单加 admin_only 标志，简化路由权限：admin 看全部，非 admin 看 admin_only=0 的全部
-- 不再依赖 sys_role_menu 做日常路由分发（细粒度权限保留作为未来扩展点）

USE life_record;

DELIMITER //

DROP PROCEDURE IF EXISTS _add_column_if_missing//

CREATE PROCEDURE _add_column_if_missing(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def  VARCHAR(255)
)
BEGIN
    DECLARE col_count INT;
    SELECT COUNT(*) INTO col_count
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl_name AND COLUMN_NAME = col_name;

    IF col_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN `', col_name, '` ', col_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DELIMITER ;

CALL _add_column_if_missing(
    'sys_menu',
    'admin_only',
    'TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否仅管理员可见：1-是 0-否'' AFTER `status`'
);

DROP PROCEDURE IF EXISTS _add_column_if_missing;

-- 系统管理目录及其子菜单标为 admin 专属
UPDATE `sys_menu` SET `admin_only` = 1 WHERE `id` IN (20, 21, 22, 23);
