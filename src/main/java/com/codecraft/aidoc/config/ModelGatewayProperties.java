package com.codecraft.aidoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 模型网关的全量配置项，涵盖开关、OpenAI 连接信息等。
 * 通过集中管理可方便地扩展到其他模型供应商。
 */
@Data
@ConfigurationProperties(prefix = "ai.gateway")
public class ModelGatewayProperties {

    /**
     * 是否启用真实的模型调用，关闭时将自动走本地启发式兜底逻辑。
     */
    private boolean enabled = false;

    /**
     * 模型供应商标识，可选值：openai、deepseek。
     */
    private String provider = "openai";

    /**
     * OpenAI 具体配置。
     */
    private final OpenAi openai = new OpenAi();

    /**
     * DeepSeek 具体配置。
     */
    private final DeepSeek deepseek = new DeepSeek();

    @Data
    public static class OpenAi {
        /**
         * OpenAI API 基础地址。
         */
        private String baseUrl = "https://api.openai.com/v1";

        /**
         * 调用所需的 API Key，通过环境变量传入以避免硬编码。
         */
        private String apiKey;

        /**
         * 使用的模型名称，例如 gpt-4o-mini。
         */
        private String model = "gpt-4o-mini";

        /**
         * 最大输出 token 数，避免生成过长文本。
         */
        private int maxOutputTokens = 512;

        /**
         * 温度参数，适度控制生成稳定性。
         */
        private double temperature = 0.15;

        /**
         * HTTP 请求超时时间，毫秒。
         */
        private int timeoutMs = 20000;
    }

    @Data
    public static class DeepSeek {
        /**
         * DeepSeek API 基础地址。
         */
        private String baseUrl = "https://api.deepseek.com/v1";

        /**
         * DeepSeek API Key。
         */
        private String apiKey;

        /**
         * 模型名称，例如 deepseek-chat。
         */
        private String model = "deepseek-chat";

        private int maxOutputTokens = 512;

        private double temperature = 0.2;

        private int timeoutMs = 20000;
    }
}
