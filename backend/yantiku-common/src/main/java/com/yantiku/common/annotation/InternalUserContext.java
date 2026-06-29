package com.yantiku.common.annotation;

/**
 * 内部服务用户上下文 — Gateway 验证 JWT 后通过 HTTP Header 传递用户 ID 和角色
 * 各服务通过 Filter 写入、通过该 ThreadLocal 读取
 */
public class InternalUserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    public static void set(Long userId) { USER_ID.set(userId); }
    public static void set(Long userId, String role) { USER_ID.set(userId); USER_ROLE.set(role); }
    public static Long getUserId() { return USER_ID.get(); }
    public static String getUserRole() { return USER_ROLE.get(); }
    public static boolean isAdmin() { return "admin".equals(USER_ROLE.get()); }
    public static void clear() { USER_ID.remove(); USER_ROLE.remove(); }
}
