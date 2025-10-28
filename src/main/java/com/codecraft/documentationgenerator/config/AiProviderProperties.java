package com.codecraft.documentationgenerator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 文档生成所使用的 AI 模型配置。
 */
@Data
@ConfigurationProperties(prefix = "docgen.ai")
public class AiProviderProperties {

    /**
     * 可选的模型提供商。
     */
    public enum Provider {
        OPENAI,
        DEEPSEEK
    }

    /**
     * 当前使用的模型提供商，默认沿用 OpenAI。
     */
    private Provider provider = Provider.OPENAI;

    /**
     * OpenAI 相关配置（目前依赖 Spring AI 自身的配置项，此处仅预留模型覆盖能力）。
     */
    private String openaiModel;

    private final DeepSeekProperties deepseek = new DeepSeekProperties();

    @Data
    public static class DeepSeekProperties {
        /**
         * DeepSeek 基础地址。
         */
        private String baseUrl = "https://api.deepseek.com";

        /**
         * chat/completions 接口使用的模型名称。
         */
        private String model = "deepseek-chat";

        /**
         * API Key，从环境变量或配置文件中注入。
         */
        private String apiKey;

        /**
         * 请求超时时间。
         */
        private Duration timeout = Duration.ofSeconds(60);

        /**
         * 默认温度值，可按需覆盖。
         */
        private Double temperature = 0.2;
    }
}
