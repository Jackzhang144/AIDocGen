package com.codecraft.documentationgenerator.entity;

import lombok.Data;

@Data
public class Team {
    private Long id;
    private String admin;
    private String members; // 改为String类型，存储JSON格式的成员列表
}