-- 会话信息全部转 Redis，sys_user_session 不再需要
-- Redis Hash 结构：key = record:user:session:{userId}，字段 sid + token，TTL = refreshTokenExpireDays

USE life_record;

DROP TABLE IF EXISTS `sys_user_session`;
