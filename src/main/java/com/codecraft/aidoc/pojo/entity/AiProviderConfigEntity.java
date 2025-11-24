package com.codecraft.aidoc.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 持久化存储 AI 提供方配置，支持在后台切换模型网关。
 */
@Data
@TableName("ai_provider_configs")
public class AiProviderConfigEntity {

    @TableId
    private Long id;

    private String provider;

    @TableField("api_key")
    private String apiKey;

    @TableField("base_url")
    private String baseUrl;

    private String model;

    private Double temperature;

    @TableField("max_output_tokens")
    private Integer maxOutputTokens;

    private Boolean enabled;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
