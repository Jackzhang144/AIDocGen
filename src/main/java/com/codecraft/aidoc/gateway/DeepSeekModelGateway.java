package com.codecraft.aidoc.gateway;

import com.codecraft.aidoc.config.ModelGatewayProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DeepSeek 模型网关，使用官方 OpenAI-Compatible API。
 */
@Slf4j
public class DeepSeekModelGateway implements ModelGateway {

    private final ModelGatewayProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI endpointUri;

    public DeepSeekModelGateway(ModelGatewayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getDeepseek().getTimeoutMs()))
                .build();
        this.endpointUri = URI.create(normalizeBaseUrl(properties.getDeepseek().getBaseUrl()) + "/chat/completions");
    }

    @Override
    public Optional<ModelGatewayResult> generateDocstring(ModelGatewayRequest request) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        String apiKey = properties.getDeepseek().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            log.warn("[AIDocGen] 已选择 DeepSeek 但未配置 API Key，自动降级");
            return Optional.empty();
        }
        try {
            Map<String, Object> payload = buildPayload(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(endpointUri)
                    .timeout(Duration.ofMillis(properties.getDeepseek().getTimeoutMs()))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();
            long start = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long latency = System.currentTimeMillis() - start;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").path(0).path("message").path("content").asText(null);
                if (!StringUtils.hasText(content)) {
                    log.warn("[AIDocGen] DeepSeek 响应缺少 content 字段，body={}", response.body());
                    return Optional.empty();
                }
                JsonNode usage = root.path("usage");
                Integer promptTokens = usage.isMissingNode() ? null : usage.path("prompt_tokens").asInt();
                Integer completionTokens = usage.isMissingNode() ? null : usage.path("completion_tokens").asInt();
                return Optional.of(ModelGatewayResult.builder()
                        .content(content.trim())
                        .provider("deepseek")
                        .latencyMs(latency)
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .fallback(false)
                        .build());
            }
            log.error("[AIDocGen] 调用 DeepSeek 失败，状态码 {}，body={}", response.statusCode(), response.body());
        } catch (Exception ex) {
            log.error("[AIDocGen] 调用 DeepSeek 接口异常，降级到本地生成", ex);
        }
        return Optional.empty();
    }

    private Map<String, Object> buildPayload(ModelGatewayRequest request) {
        ModelGatewayProperties.DeepSeek cfg = properties.getDeepseek();
        return Map.of(
                "model", cfg.getModel(),
                "temperature", cfg.getTemperature(),
                "max_tokens", cfg.getMaxOutputTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user", "content", request.getCode())
                )
        );
    }

    private String buildSystemPrompt() {
        return """
                你是深度代码文档助手，请按照调用方要求生成精准、结构化且不编造事实的中文注释。
                """;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://api.deepseek.com/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
