package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.model.GenerateDocRequest;
import com.codecraft.documentationgenerator.model.Synopsis;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Playground 接口，用于调试解析逻辑
 */
@Slf4j
@RestController
@RequestMapping("/playground")
public class PlaygroundController {

    private final CodeParsingServiceInterface codeParsingService;
    private final String adminAccessKey;

    public PlaygroundController(CodeParsingServiceInterface codeParsingService,
                                @Value("${ADMIN_ACCESS_KEY:}") String adminAccessKey) {
        this.codeParsingService = codeParsingService;
        this.adminAccessKey = adminAccessKey;
    }

    @PostMapping("/mints/{mode}")
    public ResponseEntity<?> inspect(@PathVariable String mode, @RequestBody PlaygroundRequest request) {
        validateAccessKey(request.getAccessKey());

        if ("ast".equalsIgnoreCase(mode)) {
            return ResponseEntity.ok(Map.of("ast", buildNaiveAst(request.getCode())));
        } else if ("synopsis".equalsIgnoreCase(mode)) {
            Synopsis synopsis = codeParsingService.getSynopsis(request.getCode(), request.getLanguageId(), request.getContext());
            return ResponseEntity.ok(Map.of("synopsis", synopsis));
        }

        return ResponseEntity.ok().build();
    }

    private void validateAccessKey(String accessKey) {
        if (adminAccessKey == null || adminAccessKey.isEmpty() || !adminAccessKey.equals(accessKey)) {
            throw new BusinessException("Invalid access key");
        }
    }

    private Map<String, Object> buildNaiveAst(String code) {
        Map<String, Object> node = new HashMap<>();
        node.put("type", "Program");
        List<Map<String, Object>> body = new ArrayList<>();
        if (code != null) {
            String[] lines = code.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                Map<String, Object> lineNode = new HashMap<>();
                lineNode.put("type", "Line");
                lineNode.put("value", lines[i].trim());
                lineNode.put("line", i + 1);
                body.add(lineNode);
            }
        }
        node.put("body", body);
        return node;
    }

    @Data
    public static class PlaygroundRequest {
        private String accessKey;
        private String code;
        private String languageId;
        private String context;
    }
}
