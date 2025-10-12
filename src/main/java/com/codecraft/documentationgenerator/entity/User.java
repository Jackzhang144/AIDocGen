package com.codecraft.documentationgenerator.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 表示系统用户信息
 * 对应数据库中的users表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class User {
    /**
     * 用户ID，主键
     */
    private Long id;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户密码，存储加密后的密码
     */
    private String password;

    /**
     * 用户创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 用户最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 用户刷新令牌
     */
    private String refreshToken;

    /**
     * 用户订阅计划，免费或付费计划
     */
    private String plan;

    /**
     * Stripe客户ID
     */
    private String stripeCustomerId;

    /**
     * 用户信息更新时间
     */
    private LocalDateTime updatedAt;
}