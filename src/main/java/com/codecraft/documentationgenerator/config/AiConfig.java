package com.codecraft.documentationgenerator.config;

import com.codecraft.documentationgenerator.service.ai.AiModelClient;
import com.codecraft.documentationgenerator.service.ai.DeepSeekModelClient;
import com.codecraft.documentationgenerator.service.ai.SpringAiModelClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * AI 配置类，按需选择不同的模型供应商。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "docgen.ai", name = "provider", havingValue = "OPENAI", matchIfMissing = true)
    public ChatClient chatClient(ChatClient.Builder builder, AiProviderProperties properties) {
        if (StringUtils.hasText(properties.getOpenaiModel())) {
            log.info("Configuring OpenAI chat client with model {}", properties.getOpenaiModel());
            builder = builder.defaultOptions(OpenAiChatOptions.builder()
                    .model(properties.getOpenaiModel())
                    .build());
        } else {
            log.info("Configuring OpenAI chat client with default model from Spring AI");
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "docgen.ai", name = "provider", havingValue = "DEEPSEEK")
    public RestTemplate deepSeekRestTemplate(RestTemplateBuilder builder, AiProviderProperties properties) {
        Duration timeout = properties.getDeepseek().getTimeout();
        return builder
                .rootUri(properties.getDeepseek().getBaseUrl())
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }

    @Bean
    public AiModelClient aiModelClient(AiProviderProperties properties,
                                       ObjectProvider<ChatClient> chatClientProvider,
                                       ObjectProvider<RestTemplate> deepSeekRestTemplateProvider) {
        if (properties.getProvider() == AiProviderProperties.Provider.DEEPSEEK) {
            log.info("Using DeepSeek provider for documentation generation");
            RestTemplate restTemplate = deepSeekRestTemplateProvider.getIfAvailable();
            Assert.notNull(restTemplate, "DeepSeek RestTemplate must be configured");
            return new DeepSeekModelClient(restTemplate, properties.getDeepseek());
        }
        log.info("Using OpenAI provider for documentation generation");
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        Assert.notNull(chatClient, "OpenAI ChatClient must be configured");
        return new SpringAiModelClient(chatClient);
    }
}
