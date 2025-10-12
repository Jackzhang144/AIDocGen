package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.model.Synopsis;

/**
 * AI文档生成服务接口
 * <p>
 * 提供基于AI的文档生成相关业务逻辑处理
 *
 * @author CodeCraft
 * @version 1.0
 */
public interface AiDocumentationServiceInterface {

    /**
     * 生成函数文档字符串
     *
     * @param code       代码片段
     * @param synopsis   代码概要
     * @param languageId 语言标识
     * @return 生成的文档字符串
     */
    String generateFunctionDocstring(String code, Synopsis synopsis, String languageId);

    /**
     * 生成类文档字符串
     *
     * @param code       代码片段
     * @param languageId 语言标识
     * @return 生成的文档字符串
     */
    String generateClassDocstring(String code, String languageId);

    /**
     * 生成简单代码说明
     *
     * @param code       代码片段
     * @param languageId 语言标识
     * @return 生成的说明
     */
    String generateSimpleExplanation(String code, String languageId);
}