-- 修正 sys_menu.component 指向：业务页面的 vue 文件在 src/views/{module}/index.vue
-- 而不在 src/views/business/{module}/index.vue，旧值导致前端组件找不到、页面白屏

USE life_record;

UPDATE `sys_menu` SET `component` = '/diary/index'     WHERE `id` = 11;
UPDATE `sys_menu` SET `component` = '/checkin/index'   WHERE `id` = 12;
UPDATE `sys_menu` SET `component` = '/memorial/index'  WHERE `id` = 13;
UPDATE `sys_menu` SET `component` = '/ledger/index'    WHERE `id` = 14;
