package com.codecraft.documentationgenerator.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String email;
    private String name;
    private String password; // 存储加密后的密码
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private String refreshToken;
    private String plan; // 免费或付费计划
    private String stripeCustomerId;
    private LocalDateTime updatedAt;
}