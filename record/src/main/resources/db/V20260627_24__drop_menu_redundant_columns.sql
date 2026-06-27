-- =====================================================================
-- 清理 sys_menu 冗余列：title/icon/rank/show_link/keep_alive/auths/frame_src
-- 数据已全部合并到 meta JSON 中，这些独立列不再需要
-- =====================================================================

-- 步骤 1：将旧列数据合并进 meta JSON（仅处理 meta 为 NULL 的记录）
UPDATE `sys_menu`
SET `meta` = JSON_OBJECT(
    'title',      IFNULL(`title`, ''),
    'icon',       `icon`,
    'rank',       IFNULL(`rank`, 0),
    'showLink',   IF(`show_link` = 1, true, false),
    'keepAlive',  IF(`keep_alive` = 1, true, false),
    'frameSrc',   `frame_src`
)
WHERE `meta` IS NULL;

-- 步骤 2：删除冗余列
ALTER TABLE `sys_menu`
    DROP COLUMN `title`,
    DROP COLUMN `icon`,
    DROP COLUMN `rank`,
    DROP COLUMN `show_link`,
    DROP COLUMN `keep_alive`,
    DROP COLUMN `auths`,
    DROP COLUMN `frame_src`;
