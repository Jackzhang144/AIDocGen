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
                        Map.of("role", "user", "content", buildUserPrompt(request))
                )
        );
    }

    private String buildSystemPrompt() {
        return """
                你是深度代码文档助手，请按照调用方要求生成精准、结构化且不编造事实的中文注释。
                """;
    }

    /**
     * 提供更丰富的上下文，确保 DeepSeek 生成符合预期的注释格式与内容。
     */
    private String buildUserPrompt(ModelGatewayRequest request) {
        StringBuilder builder = new StringBuilder();
        var synopsis = request.getSynopsis();
        builder.append("为下列 ").append(request.getLanguageId().getId())
                .append(" 代码生成 ").append(request.getDocFormat().getId()).append(" 风格文档，并给出必要的行级注释。\n");
        builder.append("结构类型: ").append(synopsis.getKind()).append("，名称: ").append(synopsis.getName()).append('\n');
        builder.append("参数: ").append(synopsis.getParameters()).append("，是否返回值: ").append(synopsis.isReturnsValue()).append('\n');
        builder.append("摘要: ").append(synopsis.getSummary()).append('\n');
        if (request.getQuality() != null) {
            builder.append("质量档位: ").append(request.getQuality()).append('\n');
        }
        if (request.getLineCommentRatio() != null) {
            builder.append("行级注释比例: ").append(request.getLineCommentRatio()).append('\n');
        }
        if (StringUtils.hasText(request.getContext())) {
            builder.append("上下文片段:\n").append(request.getContext()).append('\n');
        }
        builder.append("请输出纯文本，不要使用 Markdown 代码块；不得编造不存在的参数或行为。");
        if (request.isCommented()) {
            builder.append(" 生成的内容需可直接置入 ").append(request.getCommentFormat()).append(" 注释。");
        }
        if (request.getWidth() != null && request.getWidth() > 0) {
            builder.append(" 每行尽量不超过 ").append(request.getWidth()).append(" 列。");
        }
        builder.append("\n原始代码:\n").append(request.getCode());
        return builder.toString();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://api.deepseek.com/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
