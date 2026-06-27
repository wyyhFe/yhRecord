-- =====================================================================
-- 博客模块建表：文章、评论、标签关联、浏览记录
-- =====================================================================

CREATE TABLE IF NOT EXISTS `biz_blog_post` (
  `id`              BIGINT       NOT NULL COMMENT '文章 ID（雪花）',
  `user_id`         BIGINT       NOT NULL COMMENT '作者用户 ID',
  `title`           VARCHAR(256) NOT NULL COMMENT '文章标题',
  `slug`            VARCHAR(256) NOT NULL COMMENT 'URL 友好标识，英文连字符格式',
  `markdown_content` MEDIUMTEXT  NOT NULL COMMENT 'Markdown 正文',
  `html_content`    MEDIUMTEXT  NOT NULL COMMENT '渲染后的 HTML 正文',
  `summary`         VARCHAR(512) DEFAULT NULL COMMENT '摘要（列表展示用，可手动/自动生成）',
  `category`        VARCHAR(64)  DEFAULT NULL COMMENT '文章分类（自由文本，如 tech / life / reading）',
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT / PUBLISHED / ARCHIVED',
  `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '真实浏览人次（IP+用户去重）',
  `comment_count`   INT          NOT NULL DEFAULT 0 COMMENT '评论数',
  `published_at`    DATETIME     DEFAULT NULL COMMENT '发布时间（首次 PUBLISHED 时写入）',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`      BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人用户 ID',
  `updated_by`      BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人用户 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blog_post_slug` (`slug`),
  KEY `idx_blog_post_user_status` (`user_id`, `status`),
  KEY `idx_blog_post_published_at` (`published_at`),
  CONSTRAINT `chk_blog_post_status` CHECK (`status` IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客文章表';


CREATE TABLE IF NOT EXISTS `biz_comment` (
  `id`            BIGINT       NOT NULL COMMENT '评论 ID（雪花）',
  `target_type`   VARCHAR(32)  NOT NULL COMMENT '评论目标类型：BLOG_POST / DIARY',
  `target_id`     BIGINT       NOT NULL COMMENT '目标 ID（文章 ID 或日记 ID）',
  `user_id`       BIGINT       NOT NULL COMMENT '评论用户 ID',
  `parent_id`     BIGINT       DEFAULT NULL COMMENT '父评论 ID，顶级评论为空',
  `content`       VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `device_model`  VARCHAR(64)  DEFAULT NULL COMMENT '设备型号（如 iPhone 15 Pro）',
  `platform`      VARCHAR(32)  DEFAULT NULL COMMENT '平台来源：WEB / MINIAPP / ADMIN',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人用户 ID',
  `updated_by`    BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人用户 ID',
  PRIMARY KEY (`id`),
  KEY `idx_comment_target` (`target_type`, `target_id`),
  KEY `idx_comment_parent` (`parent_id`),
  KEY `idx_comment_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用评论表（博客文章 + 日记）';


CREATE TABLE IF NOT EXISTS `biz_blog_tag_rel` (
  `id`         BIGINT NOT NULL COMMENT '关联 ID（雪花）',
  `post_id`    BIGINT NOT NULL COMMENT '文章 ID',
  `tag_name`   VARCHAR(64) NOT NULL COMMENT '标签名称（自由文本，如 Java / Spring / 生活）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_blog_tag_post` (`post_id`),
  KEY `idx_blog_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客标签关联表';


CREATE TABLE IF NOT EXISTS `biz_blog_view` (
  `id`          BIGINT      NOT NULL COMMENT '记录 ID（雪花）',
  `post_id`     BIGINT      NOT NULL COMMENT '文章 ID',
  `user_id`     BIGINT      DEFAULT NULL COMMENT '浏览用户 ID（匿名浏览为空）',
  `viewer_ip`   VARCHAR(45) DEFAULT NULL COMMENT '浏览者 IP 地址',
  `viewed_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_blog_view_post` (`post_id`),
  KEY `idx_blog_view_user_ip` (`user_id`, `viewer_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客浏览记录表（用于去重计数）';


-- =====================================================================
-- sys_menu 加 platform 列，区分管理后台 vs C 端菜单
-- =====================================================================

ALTER TABLE `sys_menu`
  ADD COLUMN `platform` VARCHAR(32) NOT NULL DEFAULT 'ADMIN' COMMENT '平台：ADMIN / WEB / ALL'
  AFTER `status`;

ALTER TABLE `sys_menu`
  ADD KEY `idx_sys_menu_platform` (`platform`);

-- 双端可见的公共菜单（首页 + 记忆墙 + 日记 + AI 助手）
UPDATE `sys_menu` SET `platform` = 'ALL' WHERE `path` IN ('/', '/diary', '/memory', '/ai');
-- Web C 端博客导航
UPDATE `sys_menu` SET `platform` = 'WEB' WHERE `path` = '/posts';
-- 以下菜单保持默认 ADMIN（日记管理/打卡管理/纪念日管理/记账管理/知识库管理 仅管理后台可见）
-- /ledger, /checkin, /memorial 无需修改（默认平台为 ADMIN）


-- =====================================================================
-- 博客管理菜单（admin 端）
-- =====================================================================

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `meta`, `menu_type`, `status`, `platform`) VALUES
(60, NULL, 'BlogDir', '/blog', NULL, '{"title":"博客管理","icon":"ep:postcard","rank":16,"showLink":true}', 'DIRECTORY', 'ENABLED', 'ADMIN'),
(61, 60,  'BlogManage', '/blog', 'business/blog/index', '{"title":"文章管理","rank":1,"showLink":true,"showParent":true}', 'PAGE', 'ENABLED', 'ADMIN'),
(62, 60,  'BlogEditor', '/blog/editor', 'business/blog/editor', '{"title":"写文章","rank":2,"showLink":true,"showParent":true}', 'PAGE', 'ENABLED', 'ADMIN');

-- 给 admin（role_id=1）和 editor（role_id=2）分配博客菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 60), (1, 61), (1, 62),
(2, 60), (2, 61), (2, 62);


-- =====================================================================
-- Web C 端菜单（博客导航）
-- =====================================================================

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `meta`, `menu_type`, `status`, `platform`) VALUES
(63, NULL, 'BlogListWeb', '/posts', NULL, '{"title":"文章","icon":"ep:reading","rank":6,"showLink":true}', 'DIRECTORY', 'ENABLED', 'WEB');
