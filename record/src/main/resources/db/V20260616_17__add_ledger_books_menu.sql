-- 添加账本管理菜单

USE life_record;

-- 先清理已有数据（防止重复执行报错）
DELETE FROM `sys_role_menu` WHERE `menu_id` = 47;
DELETE FROM `sys_menu` WHERE `id` = 47;

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `title`, `icon`, `rank`, `meta`, `menu_type`, `status`) VALUES
(47, 33, 'LedgerBooks', '/ledger/books', 'business/ledger/books/index', NULL, '账本管理', NULL, 2, '{"title":"账本管理","rank":2,"showLink":true,"showParent":true,"roles":["admin","editor"]}', 'PAGE', 'ENABLED');

-- 角色关联
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(147, 1, 47),
(247, 2, 47);
