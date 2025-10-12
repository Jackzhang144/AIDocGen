package com.codecraft.documentationgenerator.exception;

/**
 * 自定义业务异常类
 * <p>
 * 用于封装业务逻辑中的异常情况
 *
 * @author CodeCraft
 * @version 1.0
 */
public class BusinessException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}