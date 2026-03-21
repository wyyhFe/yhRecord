package com.record.common.constant;

public final class RedisKeyConstants {

    public static final String USER_SESSION = "record:user:session:";
    public static final String TOKEN_BLACKLIST = "record:token:blacklist:";
    public static final String MINI_PROGRAM_ACCESS_TOKEN = "record:wechat:mini-program:access-token";
    public static final String OFFICIAL_ACCESS_TOKEN = "record:wechat:official:access-token";

    private RedisKeyConstants() {
    }
}

