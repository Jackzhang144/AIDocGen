package com.codecraft.documentationgenerator.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 生成文档请求模型类
 * <p>
 * 用于封装生成文档的请求数据
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class GenerateDocRequest {
    /**
     * 代码内容
     */
    private String code;

    /**
     * 编程语言ID
     */
    private String languageId;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 是否需要添加注释
     */
    private Boolean commented;

    /**
     * 外部用户唯一标识
     */
    private String userId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 上下文信息
     */
    private String context;

    /**
     * 请求来源
     */
    private String source;

    /**
     * 文本宽度
     */
    private Integer width;

    /**
     * 位置信息
     */
    private Integer location;

    /**
     * 行信息
     */
    private String line;

    /**
     * 是否为选择的代码
     */
    private Boolean isSelection;

    /**
     * 文档格式
     */
    private String docFormat;

    /**
     * 文档风格
     */
    private String docStyle;

    /**
     * 自定义配置
     */
    private CustomDocSettings custom;

    /**
     * 允许的Synopsis类型（预览模式使用）
     */
    private java.util.List<String> allowedKinds;

    @Data
    public static class CustomDocSettings {
        private String template;
        private String author;
        private String date;
        private String language;
    }
}
