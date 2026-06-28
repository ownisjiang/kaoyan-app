package com.yantiku.common.exception;

/**
 * 错误请求异常
 */
public class BadRequestException extends AppException {

    public BadRequestException() {
        super(ErrorCode.PARAM_ERROR);
    }

    public BadRequestException(String message) {
        super(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BadRequestException(int code, String message) {
        super(code, message);
    }
}
