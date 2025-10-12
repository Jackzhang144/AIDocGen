package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionHandlingTest {

    @Test
    void testBusinessExceptionInheritance() {
        // 测试 BusinessException 是否继承自 RuntimeException
        BusinessException exception = new BusinessException("测试异常");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testBusinessExceptionMessage() {
        String message = "业务异常消息";
        BusinessException exception = new BusinessException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testBusinessExceptionWithCause() {
        String message = "业务异常消息";
        Throwable cause = new IllegalStateException("根本原因");
        BusinessException exception = new BusinessException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}