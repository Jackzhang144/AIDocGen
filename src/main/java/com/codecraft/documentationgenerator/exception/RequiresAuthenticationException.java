package com.codecraft.documentationgenerator.exception;

import lombok.Getter;

/**
 * 需要用户认证时抛出的异常
 */
@Getter
public class RequiresAuthenticationException extends RuntimeException {
    private final String button;
    private final String displayMessage;
    private final String error;

    public RequiresAuthenticationException(String message, String button, String error) {
        super(message);
        this.displayMessage = message;
        this.button = button;
        this.error = error;
    }
}
