-- 在线重命名：为业务表添加 biz_ 前缀
-- 原理：ALTER TABLE ... RENAME TO ... 在 MySQL 8 InnoDB 下是 metadata lock 操作，不阻塞读写
-- 使用存储过程做存在性检查，避免重复执行报错
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

-- ===================== 标签模块 =====================
CALL _rename_table_if_exists('tag_template',              'biz_tag_template');
CALL _rename_table_if_exists('user_tag',                   'biz_user_tag');

-- ===================== 日记模块 =====================
CALL _rename_table_if_exists('diary',                     'biz_diary');
CALL _rename_table_if_exists('diary_media',                'biz_diary_media');
CALL _rename_table_if_exists('diary_tag_rel',              'biz_diary_tag_rel');
CALL _rename_table_if_exists('diary_comment',             'biz_diary_comment');
CALL _rename_table_if_exists('diary_like',                 'biz_diary_like');

-- ===================== 记账模块 =====================
CALL _rename_table_if_exists('ledger_book',                'biz_ledger_book');
CALL _rename_table_if_exists('ledger_entry',               'biz_ledger_entry');
CALL _rename_table_if_exists('ledger_entry_tag_rel',       'biz_ledger_entry_tag_rel');

-- ===================== 打卡模块 =====================
CALL _rename_table_if_exists('checkin_task',               'biz_checkin_task');
CALL _rename_table_if_exists('checkin_record',             'biz_checkin_record');

-- ===================== 纪念日模块 =====================
CALL _rename_table_if_exists('memorial_day',               'biz_memorial_day');

-- ===================== 回收站 =====================
CALL _rename_table_if_exists('recycle_bin',                'biz_recycle_bin');

-- ===================== 提醒模块 =====================
CALL _rename_table_if_exists('reminder_setting',           'biz_reminder_setting');
CALL _rename_table_if_exists('reminder_log',               'biz_reminder_log');

-- ===================== AI 模块 =====================
CALL _rename_table_if_exists('ai_bill_analysis_record',    'biz_ai_bill_analysis_record');
CALL _rename_table_if_exists('ai_call_log',                'biz_ai_call_log');

-- ===================== 知识库模块 =====================
CALL _rename_table_if_exists('knowledge_base',             'biz_knowledge_base');
CALL _rename_table_if_exists('knowledge_document',         'biz_knowledge_document');
CALL _rename_table_if_exists('knowledge_chunk_task',       'biz_knowledge_chunk_task');
CALL _rename_table_if_exists('knowledge_chunk',            'biz_knowledge_chunk');

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS _rename_table_if_exists;
