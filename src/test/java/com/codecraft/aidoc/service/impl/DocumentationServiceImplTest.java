package com.codecraft.aidoc.service.impl;
import com.codecraft.aidoc.gateway.ModelGateway;
import com.codecraft.aidoc.gateway.ModelGatewayRequest;
import com.codecraft.aidoc.gateway.ModelGatewayResult;
import com.codecraft.aidoc.pojo.request.DocumentGenerationRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验大模型优先生效与本地兜底能力。
 */
class DocumentationServiceImplTest {

    @Test
    void generateDocumentationViaGateway() {
        ModelGateway gateway = request -> Optional.of(ModelGatewayResult.builder()
                .content("@param a 测试")
                .provider("stub")
                .latencyMs(12L)
                .build());
        DocumentationServiceImpl documentationService = new DocumentationServiceImpl(gateway);

        DocumentGenerationRequest request = new DocumentGenerationRequest();
        request.setLanguage("java");
        request.setCode("public int add(int a, int b) {\n    return a + b;\n}");
        request.setCommented(true);
        request.setFormat("Javadoc");

        var result = documentationService.generateDocumentation(request);

        assertNotNull(result.getDocumentation());
        assertTrue(result.getDocumentation().contains("/**"));
        assertEquals("stub", result.getModelProvider());
    }

    @Test
    void fallbackToHeuristicWhenGatewayUnavailable() {
        ModelGateway gateway = request -> Optional.empty();
        DocumentationServiceImpl documentationService = new DocumentationServiceImpl(gateway);

        DocumentGenerationRequest request = new DocumentGenerationRequest();
        request.setLanguage("python");
        request.setCode("def greet(name):\n    return f'Hello {name}'");
        request.setCommented(false);
        request.setFormat("Google");

        var result = documentationService.generateDocumentation(request);

        assertTrue(result.getDocumentation().contains("Args:"));
        assertEquals("heuristic", result.getModelProvider());
    }
}
