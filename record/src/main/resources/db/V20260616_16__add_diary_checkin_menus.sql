-- 使用单一 meta 字段存储所有配置

USE life_record;

-- 1. 添加 meta JSON 字段（如果不存在）
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_menu'
    AND COLUMN_NAME = 'meta'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_menu` ADD COLUMN `meta` JSON DEFAULT NULL COMMENT ''路由元信息（JSON格式）'' AFTER `redirect`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 迁移现有数据到 meta 字段
UPDATE `sys_menu` SET `meta` = JSON_OBJECT(
    'title', title,
    'icon', icon,
    'rank', `rank`,
    'showLink', IF(show_link = 1, true, false),
    'keepAlive', IF(keep_alive = 1, true, false),
    'frameSrc', frame_src,
    'auths', auths
);

-- 3. 删除原有业务菜单
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (10, 11, 12, 13, 14, 15, 16, 17, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44, 45, 46);
DELETE FROM `sys_menu` WHERE `id` IN (10, 11, 12, 13, 14, 15, 16, 17, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44, 45, 46);

-- 4. 创建目录（同时提供 title 字段以满足 NOT NULL 约束）
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `title`, `icon`, `rank`, `meta`, `menu_type`, `status`) VALUES
(30, NULL, 'DiaryDir', '/diary', NULL, '/diary/list', '日记管理', 'ep:notebook', 10, '{"title":"日记管理","icon":"ep:notebook","rank":10,"showLink":true}', 'DIRECTORY', 'ENABLED'),
(31, NULL, 'CheckinDir', '/checkin', NULL, '/checkin/list', '打卡管理', 'ep:checked', 11, '{"title":"打卡管理","icon":"ep:checked","rank":11,"showLink":true}', 'DIRECTORY', 'ENABLED'),
(32, NULL, 'MemorialDir', '/memorial', NULL, '/memorial/list', '纪念日管理', 'ep:calendar', 12, '{"title":"纪念日管理","icon":"ep:calendar","rank":12,"showLink":true}', 'DIRECTORY', 'ENABLED'),
(33, NULL, 'LedgerDir', '/ledger', NULL, '/ledger/list', '记账管理', 'ep:wallet', 13, '{"title":"记账管理","icon":"ep:wallet","rank":13,"showLink":true}', 'DIRECTORY', 'ENABLED'),
(34, NULL, 'KnowledgeDir', '/knowledge', NULL, '/knowledge/list', '知识库管理', 'ep:collection', 14, '{"title":"知识库管理","icon":"ep:collection","rank":14,"showLink":true}', 'DIRECTORY', 'ENABLED');

-- 5. 创建页面
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `title`, `meta`, `menu_type`, `status`) VALUES
(40, 30, 'DiaryList', '/diary/list', 'business/diary/index', NULL, '日记列表', '{"title":"日记列表","rank":1,"showLink":true,"showParent":true,"roles":["admin","editor"]}', 'PAGE', 'ENABLED'),
(41, 31, 'CheckinList', '/checkin/list', 'business/checkin/index', NULL, '打卡列表', '{"title":"打卡列表","rank":1,"showLink":true,"showParent":true,"roles":["admin","editor"]}', 'PAGE', 'ENABLED'),
(42, 32, 'MemorialList', '/memorial/list', 'business/memorial/index', NULL, '纪念日列表', '{"title":"纪念日列表","rank":1,"showLink":true,"showParent":true,"roles":["admin","editor"]}', 'PAGE', 'ENABLED'),
(43, 33, 'LedgerList', '/ledger/list', 'business/ledger/index', NULL, '记账列表', '{"title":"记账列表","rank":1,"showLink":true,"showParent":true,"roles":["admin","editor"]}', 'PAGE', 'ENABLED'),
(44, 34, 'KnowledgeList', '/knowledge/list', 'business/knowledge/index', NULL, '知识库列表', '{"title":"知识库列表","rank":1,"showLink":true,"showParent":true,"roles":["admin","editor"]}', 'PAGE', 'ENABLED'),
(45, 34, 'KnowledgeRag', '/knowledge/rag', 'business/knowledge/rag/index', NULL, 'RAG 分析', '{"title":"RAG 分析","rank":2,"showLink":true,"showParent":true,"roles":["admin"]}', 'PAGE', 'ENABLED'),
(46, 34, 'KnowledgeDetail', '/knowledge/detail', 'business/knowledge/detail/index', NULL, '文档管理', '{"title":"文档管理","rank":99,"showLink":false,"roles":["admin","editor"]}', 'PAGE', 'ENABLED');

-- 6. 更新系统管理的 meta
UPDATE `sys_menu` SET `meta` = JSON_OBJECT(
    'title', title,
    'icon', icon,
    'rank', `rank`,
    'showLink', true,
    'roles', JSON_ARRAY('admin')
) WHERE `id` IN (20, 21, 22, 23);

-- 7. 角色关联
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(130, 1, 30), (131, 1, 31), (132, 1, 32), (133, 1, 33), (134, 1, 34),
(140, 1, 40), (141, 1, 41), (142, 1, 42), (143, 1, 43), (144, 1, 44), (145, 1, 45), (146, 1, 46),
(230, 2, 30), (231, 2, 31), (232, 2, 32), (233, 2, 33), (234, 2, 34),
(240, 2, 40), (241, 2, 41), (242, 2, 42), (243, 2, 43), (244, 2, 44), (245, 2, 45), (246, 2, 46);
