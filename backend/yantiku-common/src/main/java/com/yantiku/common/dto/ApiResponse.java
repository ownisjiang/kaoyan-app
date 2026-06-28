package com.yantiku.common.dto;

import com.yantiku.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应封装
 *
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 错误码，0表示成功
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 元数据，包含分页信息、时间戳等
     */
    private Meta meta;

    /**
     * 元数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        /**
         * 当前页码
         */
        private Integer page;

        /**
         * 每页大小
         */
        private Integer pageSize;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 时间戳
         */
        private String timestamp;

        /**
         * 请求ID，用于链路追踪
         */
        private String requestId;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .build();
    }

    /**
     * 成功响应（带数据和元数据）
     *
     * @param data 响应数据
     * @param meta 元数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> ok(T data, Meta meta) {
        return ApiResponse.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .meta(meta)
                .build();
    }

    /**
     * 失败响应（错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 失败响应（ErrorCode枚举）
     *
     * @param errorCode 错误码枚举
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 成功响应（无数据）
     *
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder()
                .code(0)
                .message("success")
                .build();
    }

    /**
     * 未授权响应
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .message(message)
                .build();
    }

    /**
     * 禁止访问响应
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .code(ErrorCode.FORBIDDEN.getCode())
                .message(message)
                .build();
    }
}
