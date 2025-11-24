package com.codecraft.aidoc.gateway;

import com.codecraft.aidoc.config.ModelGatewayProperties;
import com.codecraft.aidoc.enums.LanguageId;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OpenAI 实现，负责将归一化的请求转换为 chat completions API。
 */
@Slf4j
public class OpenAiModelGateway implements ModelGateway {

    private final ModelGatewayProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI endpointUri;

    public OpenAiModelGateway(ModelGatewayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getOpenai().getTimeoutMs()))
                .build();
        this.endpointUri = URI.create(normalizeBaseUrl(properties.getOpenai().getBaseUrl()) + "/chat/completions");
    }

    @Override
    public Optional<ModelGatewayResult> generateDocstring(ModelGatewayRequest request) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        String apiKey = properties.getOpenai().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            log.warn("[AIDocGen] 已启用 OpenAI 网关但未配置 API Key，将降级为本地生成");
            return Optional.empty();
        }
        try {
            Map<String, Object> payload = buildPayload(request);
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(endpointUri)
                    .timeout(Duration.ofMillis(properties.getOpenai().getTimeoutMs()))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            long start = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long latency = System.currentTimeMillis() - start;
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").path(0).path("message").path("content").asText(null);
                if (!StringUtils.hasText(content)) {
                    log.warn("[AIDocGen] OpenAI 响应缺少 content 字段，body={}", response.body());
                    return Optional.empty();
                }
                JsonNode usage = root.path("usage");
                Integer promptTokens = usage.isMissingNode() ? null : usage.path("prompt_tokens").isMissingNode() ? null : usage.path("prompt_tokens").asInt();
                Integer completionTokens = usage.isMissingNode() ? null : usage.path("completion_tokens").isMissingNode() ? null : usage.path("completion_tokens").asInt();
                return Optional.of(ModelGatewayResult.builder()
                        .content(content.trim())
                        .provider(properties.getProvider())
                        .latencyMs(latency)
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .fallback(false)
                        .build());
            }
            log.error("[AIDocGen] 调用 OpenAI 失败，状态码 {}，body={}", response.statusCode(), response.body());
        } catch (Exception ex) {
            log.error("[AIDocGen] 调用 OpenAI 接口异常，将使用本地兜底策略", ex);
        }
        return Optional.empty();
    }

    private Map<String, Object> buildPayload(ModelGatewayRequest request) {
        Map<String, Object> payload = new HashMap<>();
        ModelGatewayProperties.OpenAi cfg = properties.getOpenai();
        payload.put("model", cfg.getModel());
        payload.put("temperature", cfg.getTemperature());
        payload.put("max_tokens", cfg.getMaxOutputTokens());
        payload.put("messages", List.of(
                Map.of("role", "system", "content", buildSystemPrompt()),
                Map.of("role", "user", "content", buildUserPrompt(request))
        ));
        return payload;
    }

    private String buildSystemPrompt() {
        return """
                你是一名资深技术文档工程师，擅长生成精准的代码注释。
                请保持语言正式、简洁，必要时使用中文解释参数含义，同时遵循调用方要求的注释格式。
                在没有足够信息时要坦诚说明，不得编造代码逻辑。""";
    }

    private String buildUserPrompt(ModelGatewayRequest request) {
        StringBuilder builder = new StringBuilder();
        var synopsis = request.getSynopsis();
        builder.append("请为以下 ").append(request.getLanguageId().getId()).append(" 代码生成 ")
                .append(request.getDocFormat().getId()).append(" 风格的文档。\n");
        builder.append("代码结构类型: ").append(safe(synopsis.getKind()))
                .append("，名称: ").append(safe(synopsis.getName())).append('\n');
        builder.append("参数列表: ").append(synopsis.getParameters() == null ? List.of() : synopsis.getParameters()).append('\n');
        builder.append("摘要: ").append(safe(synopsis.getSummary())).append('\n');
        builder.append("是否返回值: ").append(synopsis.isReturnsValue()).append('\n');
        if (request.getQuality() != null) {
            builder.append("质量档位: ").append(request.getQuality()).append('\n');
        }
        if (request.getLineCommentRatio() != null) {
            builder.append("行级注释比例: ").append(request.getLineCommentRatio()).append('\n');
        }
        if (StringUtils.hasText(request.getContext())) {
            builder.append("上下文片段: \n").append(request.getContext()).append('\n');
        }
        builder.append("请输出纯文本 docstring，不要使用 Markdown 代码块。");
        if (request.isCommented()) {
            builder.append("最终文本需要能够直接嵌入到 ").append(request.getCommentFormat()).append(" 注释中。");
        }
        if (request.getWidth() != null && request.getWidth() > 0) {
            builder.append("每行尽量不超过 ").append(request.getWidth()).append(" 列。");
        }
        builder.append("请给出精炼的块级注释，必要时生成关键行的行内注释，避免冗余。");
        builder.append("\n原始代码如下：\n```\n").append(request.getCode()).append("\n```");
        return builder.toString();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://api.openai.com/v1";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "未知";
    }
}
