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
     * 文件名
     */
    private String fileName;

    /**
     * 上下文信息
     */
    private String context;

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
}