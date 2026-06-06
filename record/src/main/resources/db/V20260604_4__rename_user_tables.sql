-- 在线重命名：补齐 user / user_session 表的 sys_ 前缀
-- 原理：复用 V20260604_3 的存在性检查思路，存储过程内做存在判断，避免重复执行报错
USE life_record;

DELIMITER //

DROP PROCEDURE IF EXISTS _rename_table_if_exists//

CREATE PROCEDURE _rename_table_if_exists(
    IN old_name VARCHAR(64),
    IN new_name VARCHAR(64)
)
BEGIN
    DECLARE table_count INT;
    SELECT COUNT(*) INTO table_count
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = old_name;

    IF table_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', old_name, '` RENAME TO `', new_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DELIMITER ;

-- ===================== 用户与会话 =====================
CALL _rename_table_if_exists('user',         'sys_user');
CALL _rename_table_if_exists('user_session', 'sys_user_session');

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS _rename_table_if_exists;
