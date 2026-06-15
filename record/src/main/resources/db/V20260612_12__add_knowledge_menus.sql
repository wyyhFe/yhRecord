-- 知识库管理 / RAG 分析菜单
-- 放在业务管理（id=10）下

USE life_record;

-- 知识库管理（列表）
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `title`, `icon`, `rank`, `menu_type`, `show_link`) VALUES
(15, 10, 'KnowledgeManage', '/business/knowledge', '/business/knowledge/index', NULL, '知识库管理', 'ep/collection', 5, 'PAGE', 1),
(16, 10, 'KnowledgeDetail', '/business/knowledge/detail', '/business/knowledge/detail/index', NULL, '文档管理', NULL, 99, 'PAGE', 0),
(17, 10, 'KnowledgeRag', '/business/knowledge/rag', '/business/knowledge/rag/index', NULL, 'RAG 分析', 'ep/chat-dot-square', 6, 'PAGE', 1)
ON DUPLICATE KEY UPDATE `component` = VALUES(`component`), `icon` = VALUES(`icon`), `rank` = VALUES(`rank`), `show_link` = VALUES(`show_link`);

-- 管理员挂 15/16/17
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(115, 1, 15), (116, 1, 16), (117, 1, 17);

-- 编辑者挂 15/16/17
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(215, 2, 15), (216, 2, 16), (217, 2, 17);
