package com.codecraft.documentationgenerator.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档实体类
 * <p>
 * 表示由AI生成的代码文档信息
 * 对应数据库中的docs表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Data
public class Doc {
    /**
     * 文档ID，主键
     */
    private Long id;

    /**
     * 用户ID，外键关联users表
     */
    private Long userId;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 生成的文档内容
     */
    private String output;

    /**
     * 生成文档的提示词
     */
    private String prompt;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 生成文档耗时(毫秒)
     */
    private Integer timeToGenerate;

    /**
     * 调用API耗时(毫秒)
     */
    private Integer timeToCall;

    /**
     * 文档来源
     */
    private String source;

    /**
     * 反馈ID
     */
    private String feedbackId;

    /**
     * 用户反馈评分
     */
    private Integer feedback;

    /**
     * 是否为预览文档
     */
    private Boolean isPreview;

    /**
     * 是否接受预览文档
     */
    private Boolean hasAcceptedPreview;

    /**
     * 是否已解释
     */
    private Boolean isExplained;

    /**
     * 文档格式
     */
    private String docFormat;

    /**
     * 注释格式
     */
    private String commentFormat;

    /**
     * 代码类型(function, class等)
     */
    private String kind;

    /**
     * 是否为选择的代码
     */
    private Boolean isSelection;

    /**
     * 提示词ID
     */
    private String promptId;

    /**
     * 实际编程语言
     */
    private String actualLanguage;

    /**
     * 文档创建时间戳
     */
    private LocalDateTime timestamp;
}