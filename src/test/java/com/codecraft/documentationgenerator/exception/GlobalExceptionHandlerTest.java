package com.codecraft.documentationgenerator.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleBusinessException() {
        // Given
        String errorMessage = "测试业务异常";
        BusinessException businessException = new BusinessException(errorMessage);

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusinessException(businessException);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("code"));
        assertEquals(errorMessage, body.get("message"));
        assertNull(body.get("data"));
    }

    @Test
    void testHandleGeneralException() {
        // Given
        Exception generalException = new Exception("系统异常");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneralException(generalException);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("code"));
        assertEquals("服务器异常", body.get("message"));
        assertNull(body.get("data"));
    }

    @Test
    void testHandleBusinessExceptionWithNullMessage() {
        // Given
        BusinessException businessException = new BusinessException(null);

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusinessException(businessException);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("code"));
        assertNull(body.get("message")); // Message should be null
        assertNull(body.get("data"));
    }
}