package com.codecraft.aidoc.config;

import com.codecraft.aidoc.gateway.DeepSeekModelGateway;
import com.codecraft.aidoc.gateway.ModelGateway;
import com.codecraft.aidoc.gateway.NoopModelGateway;
import com.codecraft.aidoc.gateway.OpenAiModelGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模型网关装配配置：根据配置选择 OpenAI / DeepSeek / 本地兜底实现。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ModelGatewayConfig {

    private final ModelGatewayProperties properties;
    private final ObjectMapper objectMapper;

    @Bean
    public ModelGateway modelGateway() {
        if (!properties.isEnabled()) {
            log.info("[AIDocGen] 模型网关已关闭，使用本地启发式逻辑");
            return new NoopModelGateway();
        }
        String provider = (properties.getProvider() == null ? "openai" : properties.getProvider()).toLowerCase();
        return switch (provider) {
            case "deepseek" -> new DeepSeekModelGateway(properties, objectMapper);
            case "openai" -> new OpenAiModelGateway(properties, objectMapper);
            default -> {
                log.warn("[AIDocGen] 未识别的模型供应商 {}，自动回退", provider);
                yield new NoopModelGateway();
            }
        };
    }
}
