package com.codecraft.documentationgenerator.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * API密钥实体类
 * <p>
 * 表示用户生成的API密钥信息
 * 对应数据库中的api_keys表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class ApiKey {
    /**
     * API密钥ID，主键
     */
    private Long id;

    /**
     * 哈希后的API密钥
     */
    private String hashedKey;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * API密钥用途
     */
    private String purpose;
}