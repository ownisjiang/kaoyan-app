package com.yantiku.common.exception;

/**
 * 数据校验异常
 */
public class ValidationException extends AppException {

    public ValidationException() {
        super(ErrorCode.VALIDATION_ERROR);
    }

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR.getCode(), message);
    }

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(int code, String message) {
        super(code, message);
    }
}
