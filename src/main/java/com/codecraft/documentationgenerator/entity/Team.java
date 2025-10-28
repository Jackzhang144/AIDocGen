package com.codecraft.documentationgenerator.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队实体类
 * <p>
 * 表示用户团队信息
 * 对应数据库中的teams表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class Team {
    /**
     * 团队ID，主键
     */
    private Long id;

    /**
     * 团队管理员邮箱
     */
    private String admin;

    /**
     * 团队成员列表，存储JSON格式的成员列表
     */
    private List<String> members;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
