package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.model.Synopsis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

@Service
public class AiDocumentationService {
    
    private final ChatClient chatClient;
    
    public AiDocumentationService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * 生成函数文档字符串
     * @param code 代码片段
     * @param synopsis 代码概要
     * @param languageId 语言标识
     * @return 生成的文档字符串
     */
    public String generateFunctionDocstring(String code, Synopsis synopsis, String languageId) {
        // 构建提示词
        String promptText = String.format("%s\n%s\n###\nHere's a one sentence summary of the above function: ", 
                getLanguageCommentFormat(languageId), code);
        
        Prompt prompt = new Prompt(promptText);
        
        // 调用AI生成文档
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
    
    /**
     * 生成类文档字符串
     * @param code 代码片段
     * @param languageId 语言标识
     * @return 生成的文档字符串
     */
    public String generateClassDocstring(String code, String languageId) {
        String promptText = String.format("%s\n%s\n###\nHere's a one sentence summary of the above class: ", 
                getLanguageCommentFormat(languageId), code);
        
        Prompt prompt = new Prompt(promptText);
        
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
    
    /**
     * 生成简单代码说明
     * @param code 代码片段
     * @param languageId 语言标识
     * @return 生成的说明
     */
    public String generateSimpleExplanation(String code, String languageId) {
        String promptText = String.format("%s\n%s\n###\nQuestion: What is the above code doing?\nAnswer: ", 
                getLanguageName(languageId), code);
        
        Prompt prompt = new Prompt(promptText);
        
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
    
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