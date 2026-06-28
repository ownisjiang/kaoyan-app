package com.yantiku.common.exception;

import com.yantiku.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的异常，返回统一的API响应格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getCode());
        ApiResponse<Object> response = ApiResponse.fail(ex.getCode(), ex.getMessage());

        return new ResponseEntity<>(response, status);
    }

    /**
     * 处理参数校验异常（@Valid注解触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("参数校验失败: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.fail(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "数据校验失败"
        );
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("非法参数: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.fail(
                ErrorCode.PARAM_ERROR.getCode(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.fail(
                ErrorCode.INTERNAL_ERROR.getCode(),
                "系统繁忙，请稍后再试"
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 根据错误码映射HTTP状态码
     * <p>
     * 映射规则：
     * - 1xxxx (认证授权) → 401 Unauthorized
     * - 2xxxx (用户相关) → 404 Not Found
     * - 3xxxx (题目/练习) → 400 Bad Request
     * - 4xxxx (批改相关) → 503 Service Unavailable
     * - 6xxxx (限流) → 429 Too Many Requests
     * - 9xxxx (参数校验) → 422 Unprocessable Entity
     * - 其他 → 500 Internal Server Error
     *
     * @param errorCode 业务错误码
     * @return HTTP状态码
     */
    private HttpStatus mapErrorCodeToHttpStatus(int errorCode) {
        int prefix = errorCode / 10000;

        switch (prefix) {
            case 1:
                return HttpStatus.UNAUTHORIZED;
            case 2:
                return HttpStatus.NOT_FOUND;
            case 3:
                return HttpStatus.BAD_REQUEST;
            case 4:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case 6:
                return HttpStatus.TOO_MANY_REQUESTS;
            case 9:
                return HttpStatus.UNPROCESSABLE_ENTITY;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
