-- =====================================================================
-- 修正菜单 platform 分配
-- =====================================================================

-- 管理后台专用（仅 admin 端可见）
UPDATE `sys_menu` SET `platform` = 'ADMIN' WHERE `id` IN (31, 32, 33, 34);
-- id=31 CheckinDir  打卡管理
-- id=32 MemorialDir 纪念日管理
-- id=33 LedgerDir   记账管理
-- id=34 KnowledgeDir 知识库管理

-- 双端可见（admin + web）
UPDATE `sys_menu` SET `platform` = 'ALL'
WHERE `path` IN ('/', '/diary', '/memory', '/ai');

-- Web 端博客导航（补充 icon）
UPDATE `sys_menu`
SET `platform` = 'WEB',
    `meta` = '{"title":"文章","icon":"ep:reading","rank":6,"showLink":true}'
WHERE `id` = 63;
