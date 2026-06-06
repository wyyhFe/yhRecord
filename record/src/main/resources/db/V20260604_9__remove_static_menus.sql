-- Home（id=1）/ Dashboard（id=2）已由 admin 前端 home.ts 静态提供
-- 后端不再返回，避免动态 addRoute 时与静态路由同名冲突
-- 同时清掉 sys_role_menu 里挂在它们上的关联

USE life_record;

DELETE FROM `sys_role_menu` WHERE `menu_id` IN (1, 2);
DELETE FROM `sys_menu`      WHERE `id`      IN (1, 2);
