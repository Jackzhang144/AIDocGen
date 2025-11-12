package com.codecraft.aidoc.gateway;

import lombok.Builder;
import lombok.Data;

/**
 * 模型返回的标准结构，包含文本内容与性能指标。
 */
@Data
@Builder
public class ModelGatewayResult {

    /**
     * 模型生成的说明文本。
     */
    private String content;

    /**
     * 实际命中的模型提供方名称，用于审计。
     */
    private String provider;

    /**
     * 模型推理耗时，毫秒。
     */
    private Long latencyMs;

    /**
     * 模型返回的 Token 使用量，可选。
     */
    private Integer promptTokens;

    /**
     * 模型输出使用的 Token 数，可选。
     */
    private Integer completionTokens;

    /**
     * 标识该结果是否由本地逻辑兜底生成。
     */
    private boolean fallback;
}
