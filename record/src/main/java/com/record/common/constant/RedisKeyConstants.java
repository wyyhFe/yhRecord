package com.record.common.constant;

public final class RedisKeyConstants {

    public static final String USER_SESSION = "record:user:session:";
    public static final String TOKEN_BLACKLIST = "record:token:blacklist:";
    public static final String MINI_PROGRAM_ACCESS_TOKEN = "record:wechat:mini-program:access-token";
    public static final String OFFICIAL_ACCESS_TOKEN = "record:wechat:official:access-token";

    /** OAuth state 上下文（防 CSRF + 区分登录/绑定意图）。完整 key 为 OAUTH_STATE + {state}。 */
    public static final String OAUTH_STATE = "record:oauth:state:";

    /** 后台二维码扫码登录 token。完整 key 为 QR_LOGIN + {qrToken}，值存储 JSON 状态。 */
    public static final String QR_LOGIN = "record:qr:login:";

    private RedisKeyConstants() {
    }
}

