package com.codecraft.aidoc.enums;

import org.springframework.http.HttpStatus;

/**
 * Application wide error catalogue that standardises error responses and logging severities.
 */
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "请求参数校验失败"),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "认证失败，请检查凭证"),
    AUTHORIZATION_FAILED(HttpStatus.FORBIDDEN, "当前账号无权执行此操作"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "目标资源不存在"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "资源已存在"),
    UPSTREAM_FAILURE(HttpStatus.BAD_GATEWAY, "上游服务调用失败"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "系统内部错误"),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "功能暂未实现");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
