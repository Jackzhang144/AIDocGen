package com.codecraft.aidoc.gateway;

import com.codecraft.aidoc.config.ModelGatewayProperties;
import com.codecraft.aidoc.pojo.entity.AiProviderConfigEntity;
import com.codecraft.aidoc.service.AiProviderConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 动态模型网关：优先使用后台配置的 provider，若不存在则回退到 application 配置。
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicModelGateway implements ModelGateway {

    private final AiProviderConfigService providerConfigService;
    private final ModelGatewayProperties fallbackProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ModelGatewayResult> generateDocstring(ModelGatewayRequest request) {
        Optional<AiProviderConfigEntity> active = providerConfigService.findActive();
        if (active.isPresent()) {
            AiProviderConfigEntity cfg = active.get();
            String provider = cfg.getProvider().toLowerCase();
            ModelGatewayProperties dynamicProps = buildPropertiesFromEntity(cfg);
            return createGateway(provider, dynamicProps).generateDocstring(request);
        }
        log.info("[AIDocGen] 未找到启用的模型提供方配置，回退本地启发式生成");
        return Optional.empty();
    }

    private ModelGateway createGateway(String provider, ModelGatewayProperties props) {
        return switch (provider) {
            case "deepseek" -> new DeepSeekModelGateway(props, objectMapper);
            case "openai" -> new OpenAiModelGateway(props, objectMapper);
            default -> {
                log.warn("[AIDocGen] 未识别的模型供应商 {}，自动回退", provider);
                yield new NoopModelGateway();
            }
        };
    }

    private ModelGatewayProperties buildPropertiesFromEntity(AiProviderConfigEntity entity) {
        ModelGatewayProperties props = new ModelGatewayProperties();
        props.setEnabled(true);
        props.setProvider(entity.getProvider());
        if ("deepseek".equalsIgnoreCase(entity.getProvider())) {
            props.getDeepseek().setApiKey(entity.getApiKey());
            props.getDeepseek().setBaseUrl(entity.getBaseUrl());
            props.getDeepseek().setModel(entity.getModel());
            props.getDeepseek().setTemperature(entity.getTemperature() == null ? props.getDeepseek().getTemperature() : entity.getTemperature());
            props.getDeepseek().setMaxOutputTokens(entity.getMaxOutputTokens() == null ? props.getDeepseek().getMaxOutputTokens() : entity.getMaxOutputTokens());
        } else {
            props.getOpenai().setApiKey(entity.getApiKey());
            props.getOpenai().setBaseUrl(entity.getBaseUrl());
            props.getOpenai().setModel(entity.getModel());
            props.getOpenai().setTemperature(entity.getTemperature() == null ? props.getOpenai().getTemperature() : entity.getTemperature());
            props.getOpenai().setMaxOutputTokens(entity.getMaxOutputTokens() == null ? props.getOpenai().getMaxOutputTokens() : entity.getMaxOutputTokens());
        }
        return props;
    }
}
