package com.codecraft.aidoc.exception;

import com.codecraft.aidoc.common.ApiResponse;
import com.codecraft.aidoc.enums.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handler that converts Java exceptions to JSON responses
 * while also registering structured log entries for observability.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors coming from {@link jakarta.validation.Valid} annotated payloads.
     *
     * @param exception aggregated validation exception
     * @return HTTP 400 payload with validation message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        final String message = exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? ErrorCode.VALIDATION_FAILED.getMessage() : error.getDefaultMessage())
                .orElse(ErrorCode.VALIDATION_FAILED.getMessage());
        log.warn("[AIDocGen] 参数校验失败: {}", message);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus())
                .body(ApiResponse.error(message, ErrorCode.VALIDATION_FAILED));
    }

    /**
     * Handles constraint violations thrown by method level validation.
     *
     * @param exception validation exception
     * @return HTTP 400 payload
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        final String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(cv -> cv.getMessage() == null ? ErrorCode.VALIDATION_FAILED.getMessage() : cv.getMessage())
                .orElse(ErrorCode.VALIDATION_FAILED.getMessage());
        log.warn("[AIDocGen] 约束校验失败: {}", message);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus())
                .body(ApiResponse.error(message, ErrorCode.VALIDATION_FAILED));
    }

    /**
     * Handles domain specific business exceptions.
     *
     * @param exception business exception
     * @return structured error payload
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        final ErrorCode errorCode = exception.getErrorCode();
        log.warn("[AIDocGen] 业务异常 [{}]: {}", errorCode.name(), exception.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(exception.getMessage(), errorCode));
    }

    /**
     * Catch-all handler for unexpected errors.
     *
     * @param exception root exception
     * @return HTTP 500 payload
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUncaught(Exception exception) {
        log.error("[AIDocGen] 未处理的系统异常", exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getMessage(), ErrorCode.INTERNAL_ERROR));
    }
}
