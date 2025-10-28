package com.codecraft.documentationgenerator.exception;

/**
 * 未授权访问异常
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
