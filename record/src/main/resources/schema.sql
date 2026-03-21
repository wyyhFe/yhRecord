-- 生活记录项目初始化表结构
--
-- 说明：
-- 1. 所有主键统一使用 BIGINT。
-- 2. 业务日期优先使用 DATE，操作时间优先使用 DATETIME。
-- 3. 所有核心字段都补充了 COMMENT，便于后续直接看表结构理解含义。

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY COMMENT '用户主键 ID',
    openid VARCHAR(128) NOT NULL UNIQUE COMMENT '小程序用户唯一标识 openid',
    nickname VARCHAR(64) COMMENT '用户昵称',
    avatar_path VARCHAR(255) COMMENT '头像相对路径',
    gender VARCHAR(16) COMMENT '性别',
    official_account_openid VARCHAR(128) COMMENT '公众号 openid，用于公众号消息通道',
    birthday DATE COMMENT '生日，用于年龄展示',
    signature VARCHAR(255) COMMENT '个性签名',
    status VARCHAR(16) COMMENT '用户状态，例如 ENABLED / DISABLED',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_user_official_openid (official_account_openid)
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS user_session (
    id BIGINT PRIMARY KEY COMMENT '会话主键 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    session_id VARCHAR(64) NOT NULL COMMENT '当前登录会话 ID',
    refresh_token VARCHAR(1024) NOT NULL COMMENT '刷新令牌',
    refresh_expire_at DATETIME NOT NULL COMMENT '刷新令牌过期时间',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_user_session_user (user_id),
    UNIQUE KEY uk_user_session_session (session_id),
    INDEX idx_user_session_expire (refresh_expire_at)
) COMMENT='单设备登录会话表';

CREATE TABLE IF NOT EXISTS tag_template (
    id BIGINT PRIMARY KEY COMMENT '标签模板主键 ID',
    name VARCHAR(64) NOT NULL COMMENT '模板名称',
    color VARCHAR(32) COMMENT '模板颜色',
    icon VARCHAR(128) COMMENT '模板图标',
    module_type VARCHAR(16) NOT NULL COMMENT '所属模块，例如 DIARY / LEDGER',
    sort_order INT COMMENT '排序值',
    status VARCHAR(16) COMMENT '模板状态，例如 ENABLED / DISABLED',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_tag_template_module_status (module_type, status, sort_order)
) COMMENT='后台标签模板表';

CREATE TABLE IF NOT EXISTS user_tag (
    id BIGINT PRIMARY KEY COMMENT '用户标签主键 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    template_id BIGINT COMMENT '来源模板 ID，为空表示用户自建',
    name VARCHAR(64) NOT NULL COMMENT '标签名称',
    color VARCHAR(32) COMMENT '标签颜色',
    icon VARCHAR(128) COMMENT '标签图标',
    module_type VARCHAR(16) NOT NULL COMMENT '所属模块，例如 DIARY / LEDGER',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_user_tag_user_module (user_id, module_type),
    INDEX idx_user_tag_template (template_id)
) COMMENT='用户标签表';

CREATE TABLE IF NOT EXISTS diary (
    id BIGINT PRIMARY KEY COMMENT '日记主键 ID',
    user_id BIGINT NOT NULL COMMENT '作者用户 ID',
    title VARCHAR(128) NOT NULL COMMENT '日记标题',
    content TEXT NOT NULL COMMENT '日记正文',
    record_date DATE NOT NULL COMMENT '记录日期，表示这篇日记属于哪一天',
    weather VARCHAR(32) COMMENT '天气',
    mood VARCHAR(32) COMMENT '心情',
    visibility VARCHAR(16) NOT NULL COMMENT '可见范围，例如 PRIVATE / SHARED / PUBLIC',
    remind_at DATETIME COMMENT '单篇日记提醒时间，可选',
    location_name VARCHAR(128) COMMENT '地点名称',
    address VARCHAR(255) COMMENT '完整地址',
    province VARCHAR(64) COMMENT '省份',
    city VARCHAR(64) COMMENT '城市',
    district VARCHAR(64) COMMENT '区县',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    longitude DECIMAL(10, 7) COMMENT '经度',
    location_source_type VARCHAR(16) COMMENT '定位来源，例如 CURRENT / MANUAL',
    like_count INT DEFAULT 0 COMMENT '点赞数缓存',
    comment_count INT DEFAULT 0 COMMENT '评论数缓存',
    deleted_at DATETIME COMMENT '软删除时间，为空表示未删除',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_diary_user_date (user_id, record_date),
    INDEX idx_diary_user_visibility_date (user_id, visibility, record_date),
    INDEX idx_diary_user_deleted (user_id, deleted_at)
) COMMENT='日记主表';

CREATE TABLE IF NOT EXISTS diary_media (
    id BIGINT PRIMARY KEY COMMENT '日记附件主键 ID',
    diary_id BIGINT NOT NULL COMMENT '所属日记 ID',
    media_type VARCHAR(16) NOT NULL COMMENT '附件类型，例如 IMAGE / VIDEO',
    file_path VARCHAR(255) NOT NULL COMMENT 'OSS 相对路径或对象 key',
    sort_order INT COMMENT '排序值',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_diary_media_diary (diary_id, sort_order)
) COMMENT='日记附件表';

CREATE TABLE IF NOT EXISTS diary_tag_rel (
    id BIGINT PRIMARY KEY COMMENT '日记标签关联主键 ID',
    diary_id BIGINT NOT NULL COMMENT '日记 ID',
    tag_id BIGINT NOT NULL COMMENT '标签 ID',
    UNIQUE KEY uk_diary_tag (diary_id, tag_id),
    INDEX idx_diary_tag_tag (tag_id)
) COMMENT='日记与标签关联表';

CREATE TABLE IF NOT EXISTS diary_comment (
    id BIGINT PRIMARY KEY COMMENT '评论主键 ID',
    diary_id BIGINT NOT NULL COMMENT '所属日记 ID',
    user_id BIGINT NOT NULL COMMENT '评论用户 ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_diary_comment_diary (diary_id, created_at),
    INDEX idx_diary_comment_user (user_id)
) COMMENT='日记评论表';

CREATE TABLE IF NOT EXISTS diary_like (
    id BIGINT PRIMARY KEY COMMENT '点赞主键 ID',
    diary_id BIGINT NOT NULL COMMENT '所属日记 ID',
    user_id BIGINT NOT NULL COMMENT '点赞用户 ID',
    UNIQUE KEY uk_diary_like (diary_id, user_id),
    INDEX idx_diary_like_user (user_id)
) COMMENT='日记点赞表';

CREATE TABLE IF NOT EXISTS ledger_book (
    id BIGINT PRIMARY KEY COMMENT '账本主键 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    name VARCHAR(64) NOT NULL COMMENT '账本名称',
    description VARCHAR(255) COMMENT '账本描述',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_ledger_book_user (user_id)
) COMMENT='记账账本表';

CREATE TABLE IF NOT EXISTS ledger_entry (
    id BIGINT PRIMARY KEY COMMENT '记账流水主键 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    book_id BIGINT NOT NULL COMMENT '所属账本 ID',
    type VARCHAR(16) NOT NULL COMMENT '流水类型，例如 INCOME / EXPENSE',
    amount DECIMAL(12, 2) NOT NULL COMMENT '金额，保留两位小数',
    entry_date DATE NOT NULL COMMENT '记账日期',
    remark VARCHAR(255) COMMENT '备注',
    image_path VARCHAR(255) COMMENT '记账图片路径',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_ledger_entry_user_date (user_id, entry_date),
    INDEX idx_ledger_entry_book_date (book_id, entry_date)
) COMMENT='记账流水表';

CREATE TABLE IF NOT EXISTS ledger_entry_tag_rel (
    id BIGINT PRIMARY KEY COMMENT '流水标签关联主键 ID',
    entry_id BIGINT NOT NULL COMMENT '流水 ID',
    tag_id BIGINT NOT NULL COMMENT '标签 ID',
    UNIQUE KEY uk_ledger_entry_tag (entry_id, tag_id),
    INDEX idx_ledger_entry_tag_tag (tag_id)
) COMMENT='记账流水与标签关联表';

CREATE TABLE IF NOT EXISTS checkin_task (
    id BIGINT PRIMARY KEY COMMENT '打卡任务主键 ID',
    user_id BIGINT NOT NULL COMMENT '创建用户 ID',
    name VARCHAR(128) NOT NULL COMMENT '任务名称',
    description VARCHAR(255) COMMENT '任务描述',
    start_date DATE NOT NULL COMMENT '任务开始日期',
    status VARCHAR(16) COMMENT '任务状态，例如 ENABLED / DISABLED',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_checkin_task_user (user_id, status)
) COMMENT='打卡任务表';

CREATE TABLE IF NOT EXISTS checkin_record (
    id BIGINT PRIMARY KEY COMMENT '打卡记录主键 ID',
    task_id BIGINT NOT NULL COMMENT '所属任务 ID',
    user_id BIGINT NOT NULL COMMENT '打卡用户 ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    checked_at DATETIME NOT NULL COMMENT '实际打卡时间',
    remark VARCHAR(255) COMMENT '打卡备注',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_task_user_date (task_id, user_id, checkin_date),
    INDEX idx_checkin_record_user_date (user_id, checkin_date)
) COMMENT='打卡记录表';

CREATE TABLE IF NOT EXISTS memorial_day (
    id BIGINT PRIMARY KEY COMMENT '纪念日主键 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    title VARCHAR(128) NOT NULL COMMENT '纪念日名称',
    type VARCHAR(32) NOT NULL COMMENT '纪念日类型',
    memorial_date DATE NOT NULL COMMENT '纪念日期',
    annual_repeat BIT(1) DEFAULT 0 COMMENT '是否每年重复',
    remark VARCHAR(255) COMMENT '备注',
    remind_at DATETIME COMMENT '提醒时间',
    status VARCHAR(16) COMMENT '状态，例如 ENABLED / DISABLED',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_memorial_user_date (user_id, memorial_date),
    INDEX idx_memorial_remind (remind_at)
) COMMENT='纪念日表';

CREATE TABLE IF NOT EXISTS recycle_bin (
    id BIGINT PRIMARY KEY COMMENT '回收站主键 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型，例如 DIARY / LEDGER_ENTRY',
    resource_id BIGINT NOT NULL COMMENT '资源 ID',
    deleted_at DATETIME NOT NULL COMMENT '删除时间',
    expire_at DATETIME NOT NULL COMMENT '过期清理时间',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_recycle_user_deleted (user_id, deleted_at),
    INDEX idx_recycle_expire (expire_at),
    INDEX idx_recycle_resource (resource_type, resource_id)
) COMMENT='回收站表';

CREATE TABLE IF NOT EXISTS reminder_setting (
    id BIGINT PRIMARY KEY COMMENT '提醒设置主键 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    diary_reminder_enabled BIT(1) DEFAULT 0 COMMENT '是否启用全局日记提醒',
    mini_program_reminder_enabled BIT(1) DEFAULT 0 COMMENT '是否启用小程序订阅消息通道',
    official_account_reminder_enabled BIT(1) DEFAULT 0 COMMENT '是否启用公众号模板消息通道',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_reminder_setting_user (user_id)
) COMMENT='用户提醒设置表';

CREATE TABLE IF NOT EXISTS reminder_log (
    id BIGINT PRIMARY KEY COMMENT '提醒日志主键 ID',
    user_id BIGINT NOT NULL COMMENT '接收提醒的用户 ID',
    business_type VARCHAR(32) NOT NULL COMMENT '提醒业务类型，例如 DIARY_DAILY / MEMORIAL_DAY',
    channel VARCHAR(32) NOT NULL COMMENT '发送通道，例如 MINI_PROGRAM / OFFICIAL_ACCOUNT',
    target_id BIGINT COMMENT '关联业务对象 ID，例如纪念日 ID 或账本 ID',
    business_date DATE NOT NULL COMMENT '业务日期，用于幂等控制',
    send_status VARCHAR(32) COMMENT '发送状态，例如 SUCCESS / FAILED',
    send_message VARCHAR(255) COMMENT '发送结果说明或失败原因',
    sent_at DATETIME COMMENT '发送时间',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_reminder_log_user_date (user_id, business_date),
    INDEX idx_reminder_log_type_channel_date (business_type, channel, business_date),
    INDEX idx_reminder_log_target (target_id)
) COMMENT='提醒发送日志表';
