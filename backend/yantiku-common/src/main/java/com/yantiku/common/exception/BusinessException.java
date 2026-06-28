package com.yantiku.common.exception;

/**
 * 业务异常
 *
 * 用于业务逻辑错误，如资源不存在、状态冲突等
 */
public class BusinessException extends AppException {

    public BusinessException() {
        super(ErrorCode.INTERNAL_ERROR);
    }

    public BusinessException(String message) {
        super(ErrorCode.INTERNAL_ERROR.getCode(), message);
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }
}
