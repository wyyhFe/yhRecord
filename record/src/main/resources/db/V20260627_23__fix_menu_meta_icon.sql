-- =====================================================================
-- 修正 sys_menu meta JSON：补全 icon
-- 之前 INSERT 用的独立 icon 列，但代码实际从 meta JSON 读取
-- =====================================================================

-- 博客管理目录 + ep:postcard
UPDATE `sys_menu`
SET `meta` = '{"title":"博客管理","icon":"ep:postcard","rank":16,"showLink":true}'
WHERE `id` = 60;
