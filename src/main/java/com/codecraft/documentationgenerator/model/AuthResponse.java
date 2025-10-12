package com.codecraft.documentationgenerator.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证响应模型类
 * <p>
 * 用于封装用户认证响应数据
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class AuthResponse {
    /**
     * JWT令牌
     */
    private String token;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 构造函数
     *
     * @param token   JWT令牌
     * @param message 响应消息
     */
    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
}