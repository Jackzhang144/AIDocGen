package com.codecraft.documentationgenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 * <p>
 * 配置AI相关组件，包括聊天客户端等
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Configuration
public class AiConfig {

    /**
     * 创建ChatClient Bean
     * <p>
     * 构建并返回一个ChatClient实例，用于与AI模型交互
     *
     * @param builder ChatClient构建器
     * @return ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        log.info("Initializing ChatClient bean");
        return builder.build();
    }
}