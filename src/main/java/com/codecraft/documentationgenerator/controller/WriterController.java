package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.aop.RequireLogin;
import com.codecraft.documentationgenerator.model.GenerateDocRequest;
import com.codecraft.documentationgenerator.model.Synopsis;
import com.codecraft.documentationgenerator.service.AiDocumentationServiceInterface;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import com.codecraft.documentationgenerator.service.DocServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档生成控制器
 * <p>
 * 处理AI文档生成相关的RESTful API请求
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/writer")
public class WriterController {

    @Autowired
    private CodeParsingServiceInterface codeParsingService;

    @Autowired
    private AiDocumentationServiceInterface aiDocumentationService;

    @Autowired
    private DocServiceInterface docService;

    /**
     * 生成代码文档
     * <p>
     * 根据提供的代码和上下文信息，使用AI生成相应的文档
     *
     * @param request 生成文档请求对象
     * @return String 生成的文档字符串
     */
    @RequireLogin
    @PostMapping("/write/v3")
    public String generateDocumentation(@RequestBody GenerateDocRequest request) {
        log.info("Generating documentation for code in language: {}", request.getLanguageId());

        // 解析代码获取概要信息
        Synopsis synopsis = codeParsingService.getSynopsis(
                request.getCode(),
                request.getLanguageId(),
                request.getContext());
        log.debug("Code synopsis kind: {}", synopsis.getKind());

        // 根据代码类型生成文档
        String docstring;
        switch (synopsis.getKind()) {
            case "function":
                docstring = aiDocumentationService.generateFunctionDocstring(
                        request.getCode(), synopsis, request.getLanguageId());
                break;
            case "class":
                docstring = aiDocumentationService.generateClassDocstring(
                        request.getCode(), request.getLanguageId());
                break;
            default:
                docstring = aiDocumentationService.generateSimpleExplanation(
                        request.getCode(), request.getLanguageId());
                break;
        }

        log.info("Documentation generated successfully");

        // 这里应该保存文档到数据库
        // 简化实现，直接返回生成的文档

        return docstring;
    }

    /**
     * 无选择代码生成文档
     * <p>
     * 当用户没有选择特定代码时，从上下文中提取代码并生成文档
     *
     * @param request 生成文档请求对象
     * @return String 生成的文档字符串
     */
    @RequireLogin
    @PostMapping("/write/v3/no-selection")
    public String generateDocumentationNoSelection(@RequestBody GenerateDocRequest request) {
        log.info("Generating documentation without code selection in language: {}", request.getLanguageId());

        // 如果没有选择代码，则从上下文中提取代码
        String code = codeParsingService.getCode(
                request.getContext(),
                request.getLanguageId(),
                request.getLocation(),
                request.getLine());
        log.debug("Extracted code from context, length: {}", code.length());

        request.setCode(code);
        return generateDocumentation(request);
    }
}