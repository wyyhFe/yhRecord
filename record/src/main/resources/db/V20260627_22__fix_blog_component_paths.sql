-- =====================================================================
-- 修正博客管理菜单：数据库已执行旧版 V20，组件路径不匹配实际文件
-- =====================================================================

-- id=61：文章列表页，组件路径 business/blog/index（非 post/index）
UPDATE `sys_menu`
SET `name` = 'BlogManage',
    `path` = '/blog',
    `component` = 'business/blog/index',
    `meta` = '{"title":"文章管理","rank":1,"showLink":true,"showParent":true}'
WHERE `id` = 61;

-- id=62：改为写文章编辑器页（非评论管理，评论管理页后续再做）
UPDATE `sys_menu`
SET `name` = 'BlogEditor',
    `path` = '/blog/editor',
    `component` = 'business/blog/editor',
    `meta` = '{"title":"写文章","rank":2,"showLink":true,"showParent":true}'
WHERE `id` = 62;

-- id=63：Web 端博客导航，补充 icon
UPDATE `sys_menu`
SET `meta` = '{"title":"文章","icon":"ep:reading","rank":6,"showLink":true}'
WHERE `id` = 63;

-- 确保首页 / 记忆墙 / AI 助手在 Web 端可见（platform=ALL）
UPDATE `sys_menu` SET `platform` = 'ALL'
WHERE `path` IN ('/', '/memory', '/ai')
  AND (`platform` IS NULL OR `platform` != 'ALL');
