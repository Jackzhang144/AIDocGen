package com.codecraft.documentationgenerator.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册请求模型类
 * <p>
 * 用于封装用户注册请求数据
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class RegisterRequest {
    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户姓名
     */
    private String name;
}