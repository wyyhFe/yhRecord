package com.record.common.context;

public final class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> OPEN_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> SESSION_ID_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String openId, String sessionId) {
        USER_ID_HOLDER.set(userId);
        OPEN_ID_HOLDER.set(openId);
        SESSION_ID_HOLDER.set(sessionId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static String getOpenId() {
        return OPEN_ID_HOLDER.get();
    }

    public static String getSessionId() {
        return SESSION_ID_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        OPEN_ID_HOLDER.remove();
        SESSION_ID_HOLDER.remove();
    }
}
