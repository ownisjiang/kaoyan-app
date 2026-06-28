package com.yantiku.common.annotation;

/**
 * 内部服务用户上下文 — Gateway 验证 JWT 后通过 HTTP Header 传递用户 ID
 * 各服务通过 Filter 写入、通过该 ThreadLocal 读取
 */
public class InternalUserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void set(Long userId) { USER_ID.set(userId); }
    public static Long getUserId() { return USER_ID.get(); }
    public static void clear() { USER_ID.remove(); }
}
