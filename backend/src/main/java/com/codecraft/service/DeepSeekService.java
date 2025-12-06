package com.codecraft.service;

import com.codecraft.entity.RequestLog;
import com.codecraft.repository.RequestLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class DeepSeekService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.timeout-ms:20000}")
    private long apiTimeoutMs;

    private final WebClient webClient;
    private final RequestLogRepository logRepository;

    public DeepSeekService(WebClient.Builder webClientBuilder, RequestLogRepository logRepository) {
        this.webClient = webClientBuilder.build();
        this.logRepository = logRepository;
    }

    public Mono<String> callAi(String type,
                               String code,
                               String fileName,
                               String context,
                               String language,
                               String username,
                               String filePath,
                               String framework,
                               String editorLanguage) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("调用被拒绝：未配置 DEEPSEEK_API_KEY");
            return Mono.just("未配置 DEEPSEEK_API_KEY，请在环境变量或 application.yml 中设置。");
        }
        // 调试用：打印 key 前缀确认环境变量是否注入，避免泄露全量
        log.info("DEEPSEEK_API_KEY prefix={}", apiKey.length() > 6 ? apiKey.substring(0, 6) : apiKey);

        // 1. 简单记录日志 (实际生产中建议异步处理)
        try {
            RequestLog log = new RequestLog();
            log.setRequestType(type);
            log.setFileName(fileName);
            log.setUsername(username);
            // 截取前500字符以防过长
            log.setPromptSnippet(code != null && code.length() > 500 ? code.substring(0, 500) : code);
            logRepository.save(log);
        } catch (Exception e) {
            log.warn("日志记录失败: {}", e.getMessage());
        }

        // 2. 构建 Prompt
        String langHint = getLanguageHint(language);
        String systemPrompt = getSystemPrompt(type, langHint);
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("File Name: ").append(fileName).append("\n");
        if (filePath != null && !filePath.isBlank()) {
            userPrompt.append("File Path: ").append(filePath).append("\n");
        }
        if (framework != null && !framework.isBlank()) {
            userPrompt.append("Framework/Stack: ").append(framework).append("\n");
        }
        if (editorLanguage != null && !editorLanguage.isBlank()) {
            userPrompt.append("Editor Language: ").append(editorLanguage).append("\n");
        }
        userPrompt.append("\nCode Content:\n").append(code);
        if ("document".equals(type) && context != null) {
            userPrompt.append("\n\nProject Context:\n").append(context);
        }
        if ("test".equals(type) && context != null) {
            userPrompt.append("\n\nSource File Context For Tests:\n").append(context);
        }
        log.info("调用 DeepSeek，type={}, fileName={}, codeLength={}", type, fileName, code == null ? 0 : code.length());

        // 3. 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        body.put("temperature", 0.1);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt.toString())
        ));

        // 4. 发起调用
        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(bodyStr -> Mono.error(new RuntimeException(
                                        "DeepSeek 调用失败: " + resp.statusCode() + " " + bodyStr))))
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(apiTimeoutMs))
                .map(resp -> {
                    try {
                        List<?> choices = (List<?>) resp.get("choices");
                        if (choices == null || choices.isEmpty()) return "AI 无响应";
                        Map<?, ?> first = (Map<?, ?>) choices.get(0);
                        Map<?, ?> message = (Map<?, ?>) first.get("message");
                        return (String) message.get("content");
                    } catch (Exception e) {
                        return "解析错误: " + e.getMessage();
                    }
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    String responseBody = ex.getResponseBodyAsString();
                    String message = "DeepSeek 请求失败，状态码 " + ex.getStatusCode() + "，请检查 DEEPSEEK_API_KEY 是否有效。"
                            + (Objects.nonNull(responseBody) ? (" 响应: " + responseBody) : "");
                    log.error(message);
                    return Mono.just(message);
                })
                .onErrorResume(java.util.concurrent.TimeoutException.class, ex -> {
                    String message = "调用 DeepSeek 超时，请稍后重试或检查网络。";
                    log.error(message);
                    return Mono.just(message);
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("DeepSeek 调用异常: {}", ex.getMessage(), ex);
                    return Mono.just("调用 DeepSeek 失败: " + ex.getMessage());
                });
    }

    private String getSystemPrompt(String type, String langHint) {
        return switch (type) {
            case "comment" -> """
                    你是高级代码审查与注释助手。
                    仅处理用户选中的代码片段，并在其中添加简洁、精准的行内注释或 JavaDoc。
                    保持原有代码结构与缩进，只返回修改后的代码片段本身，不要添加 ``` 包裹、不要扩写未选中的代码。
                    注释语言遵循用户要求，点到即止，避免冗长。
                    """ + langHint;
            case "explain" -> """
                    你是资深工程师，解释用户选中的代码片段。
                    输出 100-150 字，Markdown 格式，需包含：
                    - 功能/意图
                    - 关键逻辑与输入/输出
                    - 潜在风险或边界
                    只基于提供的片段，不要推测未给出的上下文。
                    """ + langHint;
            case "document" -> """
                    你是技术文档工程师，需要为“单个文件”生成简洁 Markdown 文档。
                    基于提供的文件内容，总结：
                    - 该文件的功能/职责
                    - 主要公开接口、函数或类，以及参数/返回值
                    - 关键业务流程或调用关系（如有）
                    - 潜在风险或使用注意事项
                    产出限制在 150-250 字，结构化小标题。
                    """ + langHint;
            case "rewrite" -> """
                    你是代码重写与修复助手。
                    基于用户提供的片段，返回改进后的完整代码片段。
                    要求：
                    - 保留原始接口/方法签名与必要的导入。
                    - 强调可读性与健壮性，修正潜在 bug。
                    - 输出以 diff 风格呈现，前缀 -/+ 表示删除/新增，未变更行保留原内容前缀空格。
                    - 不要添加 ``` 包裹。
                    """ + langHint;
            case "test" -> """
                    你是测试生成助手。
                    根据提供的源文件内容，给出 3-6 个针对性的测试案例草稿，包含：
                    - 用例名称/描述
                    - 输入与期望输出/断言
                    - 覆盖的边界情况
                    生成 JUnit5 或对应语言常用测试伪代码，避免依赖具体项目工具。
                    """ + langHint;
            default -> "你是专业的代码助手。" + langHint;
        };
    }

    private String getLanguageHint(String language) {
        if ("en".equalsIgnoreCase(language)) {
            return "\nPlease respond in English.";
        }
        return "\n请使用中文输出。";
    }
}
