-- 菜单与权限系统（sys_ 前缀）
-- 支持动态路由：菜单管理、角色管理、角色-菜单关联

-- ==================== 角色表 ====================
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`          BIGINT       NOT NULL COMMENT '角色 ID',
  `name`        VARCHAR(64)  NOT NULL COMMENT '角色名称（如 admin、editor）',
  `label`       VARCHAR(64)  NOT NULL COMMENT '角色显示名（如 管理员、编辑者）',
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED / DISABLED',
  `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`  BIGINT       DEFAULT NULL,
  `updated_by`  BIGINT       DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ==================== 菜单表 ====================
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id`          BIGINT       NOT NULL COMMENT '菜单 ID',
  `parent_id`   BIGINT       DEFAULT NULL COMMENT '父菜单 ID（NULL 为顶级）',
  `name`        VARCHAR(64)  NOT NULL COMMENT '路由 name（唯一标识）',
  `path`        VARCHAR(256) NOT NULL COMMENT '路由 path',
  `component`   VARCHAR(256) DEFAULT NULL COMMENT '前端组件路径（如 /business/diary，对应 src/views/business/diary/index.vue）',
  `redirect`    VARCHAR(256) DEFAULT NULL COMMENT '重定向路径',
  `title`       VARCHAR(64)  NOT NULL COMMENT '菜单显示标题',
  `icon`        VARCHAR(64)  DEFAULT NULL COMMENT '菜单图标',
  `rank`        INT          NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `menu_type`   VARCHAR(20)  NOT NULL DEFAULT 'PAGE' COMMENT '类型：DIRECTORY（目录）/ PAGE（页面）/ BUTTON（按钮权限）',
  `frame_src`   VARCHAR(512) DEFAULT NULL COMMENT '内嵌 iframe 地址',
  `show_link`   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否在菜单中显示：1-显示 0-隐藏',
  `keep_alive`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否缓存：1-是 0-否',
  `auths`       VARCHAR(512) DEFAULT NULL COMMENT '按钮权限标识（逗号分隔，如 btn:add,btn:edit）',
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED / DISABLED',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`  BIGINT       DEFAULT NULL,
  `updated_by`  BIGINT       DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_menu_name` (`name`),
  KEY `idx_sys_menu_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- ==================== 用户-角色关联表 ====================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id`       BIGINT NOT NULL COMMENT 'ID',
  `user_id`  BIGINT NOT NULL COMMENT '用户 ID',
  `role_id`  BIGINT NOT NULL COMMENT '角色 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
  KEY `idx_sys_user_role_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户-角色关联表';

-- ==================== 角色-菜单关联表 ====================
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id`       BIGINT NOT NULL COMMENT 'ID',
  `role_id`  BIGINT NOT NULL COMMENT '角色 ID',
  `menu_id`  BIGINT NOT NULL COMMENT '菜单 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu` (`role_id`, `menu_id`),
  KEY `idx_sys_role_menu_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色-菜单关联表';

-- ==================== 初始数据 ====================

-- 默认角色：管理员
INSERT INTO `sys_role` (`id`, `name`, `label`, `remark`) VALUES
(1, 'admin', '管理员', '拥有所有权限'),
(2, 'editor', '编辑者', '可管理业务数据，不可管理系统配置');

-- 默认菜单：根据现有前端路由配置
-- 首页
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `title`, `icon`, `rank`, `menu_type`, `show_link`) VALUES
(1, NULL, 'Home', '/', NULL, '/dashboard', '首页', 'ep/home-filled', 0, 'DIRECTORY', 1),
(2, 1, 'Dashboard', '/dashboard', '/dashboard/index', NULL, '仪表盘', NULL, 1, 'PAGE', 1);

-- 业务管理
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `title`, `icon`, `rank`, `menu_type`, `show_link`) VALUES
(10, NULL, 'Business', '/business', NULL, '/business/diary', '业务管理', 'ep/document', 10, 'DIRECTORY', 1),
(11, 10, 'Diary', '/business/diary', '/business/diary/index', NULL, '日记管理', 'ep/notebook', 1, 'PAGE', 1),
(12, 10, 'Checkin', '/business/checkin', '/business/checkin/index', NULL, '打卡管理', 'ep/circle-check', 2, 'PAGE', 1),
(13, 10, 'Memorial', '/business/memorial', '/business/memorial/index', NULL, '纪念日管理', 'ep/calendar', 3, 'PAGE', 1),
(14, 10, 'Ledger', '/business/ledger', '/business/ledger/index', NULL, '记账管理', 'ep/money', 4, 'PAGE', 1);

-- 系统管理
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `title`, `icon`, `rank`, `menu_type`, `show_link`) VALUES
(20, NULL, 'System', '/system', NULL, '/system/menu', '系统管理', 'ep/setting', 99, 'DIRECTORY', 1),
(21, 20, 'MenuManage', '/system/menu', '/system/menu/index', NULL, '菜单管理', 'ep/menu', 1, 'PAGE', 1),
(22, 20, 'RoleManage', '/system/role', '/system/role/index', NULL, '角色管理', 'ep/user', 2, 'PAGE', 1),
(23, 20, 'UserManage', '/system/user', '/system/user/index', NULL, '用户管理', 'ep/avatar', 3, 'PAGE', 1);

-- 管理员拥有所有菜单
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) SELECT 100 + n, 1, n FROM (
  SELECT 1 AS n UNION SELECT 2 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
  UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23
) t;

-- 编辑者只有业务管理菜单
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(200, 2, 1), (201, 2, 2),
(210, 2, 10), (211, 2, 11), (212, 2, 12), (213, 2, 13), (214, 2, 14);
