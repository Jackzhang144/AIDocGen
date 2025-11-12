package com.codecraft.aidoc.exception;

import com.codecraft.aidoc.enums.ErrorCode;
import lombok.Getter;

/**
 * Runtime exception carrying an {@link ErrorCode} so that the global exception handler
 * can translate it into a predictable HTTP response.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
