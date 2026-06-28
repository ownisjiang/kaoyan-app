package com.yantiku.common.exception;

/**
 * 资源未找到异常
 */
public class NotFoundException extends AppException {

    public NotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND.getCode(), message);
    }

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(int code, String message) {
        super(code, message);
    }
}
