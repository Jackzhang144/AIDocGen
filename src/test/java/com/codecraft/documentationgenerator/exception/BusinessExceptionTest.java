package com.codecraft.documentationgenerator.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BusinessExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "测试异常消息";
        BusinessException exception = new BusinessException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "测试异常消息";
        Throwable cause = new IllegalArgumentException("原因异常");
        BusinessException exception = new BusinessException(message, cause);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}