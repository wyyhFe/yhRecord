-- 后台用户合并：sys_user 加 merged_into_user_id 字段
-- 合并后 source 用户置为 DISABLED 并写入 merged_into_user_id，保留历史便于审计/回滚

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
    'sys_user',
    'merged_into_user_id',
    'BIGINT DEFAULT NULL COMMENT ''若用户被合并到其他账号，此字段保存目标用户 ID'' AFTER `status`'
);

DROP PROCEDURE IF EXISTS _add_column_if_missing;
