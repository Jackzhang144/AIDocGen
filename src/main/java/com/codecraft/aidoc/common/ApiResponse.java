package com.codecraft.aidoc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardised envelope for HTTP JSON responses so that the client can rely on a consistent shape.
 *
 * @param <T> type of the payload that accompanies the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 业务状态码，0 表示成功，其余值参考 {@link com.codecraft.aidoc.enums.ErrorCode#getStatus()}。
     */
    @Builder.Default
    private int code = 0;

    /**
     * Human readable message describing the outcome.
     */
    private String message;

    /**
     * Optional payload. When absent the client should only rely on {@link #message}.
     */
    private T data;

    /**
     * Flag indicating success status. This helps consumers avoid relying on HTTP status codes only.
     */
    @Builder.Default
    private boolean success = true;

    /**
     * Builds a successful response with the supplied payload.
     *
     * @param message user facing message
     * @param data    payload to return
     * @param <T>     payload type
     * @return response instance
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Builds an error response.
     *
     * @param message human readable error description
     * @param <T>     payload type
     * @return response instance
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(com.codecraft.aidoc.enums.ErrorCode.INTERNAL_ERROR.getStatus().value())
                .success(false)
                .message(message)
                .build();
    }

    /**
     * Builds an error response with explicit {@link com.codecraft.aidoc.enums.ErrorCode}.
     */
    public static <T> ApiResponse<T> error(String message, com.codecraft.aidoc.enums.ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .code(errorCode.getStatus().value())
                .success(false)
                .message(message)
                .build();
    }
}
