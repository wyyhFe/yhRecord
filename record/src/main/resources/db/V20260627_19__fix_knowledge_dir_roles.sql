-- 修复 KnowledgeDir 缺少 roles 限制，导致匿名 C 端可见空壳目录
UPDATE sys_menu
SET meta = '{"title":"知识库管理","icon":"ep:collection","rank":14,"showLink":true,"roles":["admin","editor"]}'
WHERE id = 34;
