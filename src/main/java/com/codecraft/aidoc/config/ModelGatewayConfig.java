package com.codecraft.aidoc.config;

import com.codecraft.aidoc.gateway.DeepSeekModelGateway;
import com.codecraft.aidoc.gateway.DynamicModelGateway;
import com.codecraft.aidoc.gateway.ModelGateway;
import com.codecraft.aidoc.gateway.NoopModelGateway;
import com.codecraft.aidoc.gateway.OpenAiModelGateway;
import com.codecraft.aidoc.pojo.entity.AiProviderConfigEntity;
import com.codecraft.aidoc.service.AiProviderConfigService;
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
    private final AiProviderConfigService providerConfigService;

    @Bean
    public ModelGateway modelGateway() {
        return new DynamicModelGateway(providerConfigService, properties, objectMapper);
    }
}
