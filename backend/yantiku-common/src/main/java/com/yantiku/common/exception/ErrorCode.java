package com.yantiku.common.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 错误码规则：
 * - 0: 成功
 * - 10001-19999: 认证授权相关
 * - 20001-29999: 用户相关
 * - 30001-39999: 题目/练习相关
 * - 40001-49999: 批改相关
 * - 50001-59999: 系统错误
 * - 60001-69999: 限流相关
 * - 70001-79999: 消息队列相关
 * - 90001-99999: 参数校验相关
 */
@Getter
public enum ErrorCode {
    // 成功
    SUCCESS(0, "成功"),

    // 认证授权 (10001-19999)
    UNAUTHORIZED(10001, "未授权"),
    TOKEN_EXPIRED(10002, "令牌已过期"),
    INVALID_TOKEN(10003, "无效的令牌"),
    PASSWORD_ERROR(10004, "密码错误"),
    PHONE_REGISTERED(10005, "手机号已注册"),
    LOGIN_FAILED(10006, "登录失败"),
    PHONE_ALREADY_REGISTERED(10007, "手机号已注册"),
    REFRESH_TOKEN_INVALID(10008, "刷新令牌无效"),
    REFRESH_TOKEN_REVOKED(10009, "刷新令牌已撤销"),
    REFRESH_TOKEN_EXPIRED(10010, "刷新令牌已过期"),
    FORBIDDEN(10011, "禁止访问"),
    VERIFICATION_CODE_ERROR(10012, "验证码错误或已过期"),

    // 用户 (20001-29999)
    USER_NOT_FOUND(20001, "用户不存在"),
    ACCOUNT_DISABLED(20002, "账号已禁用"),
    USER_ACCOUNT_DISABLED(20003, "账号已禁用"),

    // 题目/练习 (30001-39999)
    QUESTION_NOT_FOUND(30001, "题目不存在"),
    SESSION_EXPIRED(30002, "练习会话已过期"),
    SESSION_COMPLETED(30003, "练习会话已完成"),

    // 批改 (40001-49999)
    GRADING_TASK_NOT_FOUND(40001, "批改任务不存在"),
    GRADING_IN_PROGRESS(40002, "批改进行中"),
    GRADING_FAILED(40003, "批改失败"),

    // 系统错误 (50001-59999)
    INTERNAL_ERROR(50001, "系统内部错误"),
    DB_ERROR(50002, "数据库错误"),

    // 消息队列 (70001-79999)
    RABBITMQ_ERROR(70001, "消息队列错误"),

    // 限流 (60001-69999)
    RATE_LIMIT(60001, "请求过于频繁，请稍后再试"),

    // 参数校验 (90001-99999)
    VALIDATION_ERROR(90001, "数据校验失败"),
    PARAM_ERROR(90002, "参数错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
