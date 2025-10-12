package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.model.GenerateDocRequest;
import com.codecraft.documentationgenerator.model.Synopsis;
import com.codecraft.documentationgenerator.service.AiDocumentationService;
import com.codecraft.documentationgenerator.service.CodeParsingService;
import com.codecraft.documentationgenerator.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/writer")
public class WriterController {

    @Autowired
    private CodeParsingService codeParsingService;

    @Autowired
    private AiDocumentationService aiDocumentationService;

    @Autowired
    private DocService docService;

    @PostMapping("/write/v3")
    public String generateDocumentation(@RequestBody GenerateDocRequest request) {
        // 解析代码获取概要信息
        Synopsis synopsis = codeParsingService.getSynopsis(
                request.getCode(),
                request.getLanguageId(),
                request.getContext());

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

        // 这里应该保存文档到数据库
        // 简化实现，直接返回生成的文档

        return docstring;
    }

    @PostMapping("/write/v3/no-selection")
    public String generateDocumentationNoSelection(@RequestBody GenerateDocRequest request) {
        // 如果没有选择代码，则从上下文中提取代码
        String code = codeParsingService.getCode(
                request.getContext(),
                request.getLanguageId(),
                request.getLocation(),
                request.getLine());

        request.setCode(code);
        return generateDocumentation(request);
    }
}