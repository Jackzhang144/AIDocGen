package com.codecraft.documentationgenerator.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证请求模型类
 * <p>
 * 用于封装用户登录请求数据
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class AuthRequest {
    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户密码
     */
    private String password;
}