package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.model.Synopsis;
import com.codecraft.documentationgenerator.service.AiDocumentationServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * AI文档生成服务实现类
 * <p>
 * 实现基于AI的文档生成相关业务逻辑
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Service
public class AiDocumentationServiceImpl implements AiDocumentationServiceInterface {

    private final ChatClient chatClient;

    /**
     * 构造函数
     *
     * @param chatClient AI聊天客户端
     */
    public AiDocumentationServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 生成函数文档字符串
     *
     * @param code       代码片段
     * @param synopsis   代码概要
     * @param languageId 语言标识
     * @return 生成的文档字符串
     */
    public String generateFunctionDocstring(String code, Synopsis synopsis, String languageId) {
        log.info("Generating function docstring for language: {}", languageId);

        // 构建提示词
        String promptText = String.format("%s\n%s\n###\nHere's a one sentence summary of the above function: ",
                getLanguageCommentFormat(languageId), code);

        Prompt prompt = new Prompt(promptText);

        // 调用AI生成文档
        String result = chatClient.prompt(prompt).call().content();
        log.debug("Function docstring generated successfully, length: {}", result.length());
        return result;
    }

    /**
     * 生成类文档字符串
     *
     * @param code       代码片段
     * @param languageId 语言标识
     * @return 生成的文档字符串
     */
    public String generateClassDocstring(String code, String languageId) {
        log.info("Generating class docstring for language: {}", languageId);

        String promptText = String.format("%s\n%s\n###\nHere's a one sentence summary of the above class: ",
                getLanguageCommentFormat(languageId), code);

        Prompt prompt = new Prompt(promptText);

        String result = chatClient.prompt(prompt).call().content();
        log.debug("Class docstring generated successfully, length: {}", result.length());
        return result;
    }

    /**
     * 生成简单代码说明
     *
     * @param code       代码片段
     * @param languageId 语言标识
     * @return 生成的说明
     */
    public String generateSimpleExplanation(String code, String languageId) {
        log.info("Generating simple explanation for language: {}", languageId);

        String promptText = String.format("%s\n%s\n###\nQuestion: What is the above code doing?\nAnswer: ",
                getLanguageName(languageId), code);

        Prompt prompt = new Prompt(promptText);

        String result = chatClient.prompt(prompt).call().content();
        log.debug("Simple explanation generated successfully, length: {}", result.length());
        return result;
    }

    /**
     * 获取语言注释格式
     *
     * @param languageId 语言标识
     * @return 注释格式字符串
     */
    private String getLanguageCommentFormat(String languageId) {
        switch (languageId) {
            case "java":
                return "/**\n * \n */";
            case "python":
                return "#";
            case "javascript":
                return "/**\n * \n */";
            default:
                return "/* */";
        }
    }

    /**
     * 获取语言名称
     *
     * @param languageId 语言标识
     * @return 语言名称
     */
    private String getLanguageName(String languageId) {
        switch (languageId) {
            case "java":
                return "Java";
            case "python":
                return "Python";
            case "javascript":
                return "JavaScript";
            default:
                return "Code";
        }
    }
}