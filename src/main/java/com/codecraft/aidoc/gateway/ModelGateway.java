package com.codecraft.aidoc.gateway;

import java.util.Optional;

/**
 * 对接不同大模型供应商的统一抽象，负责完成 Prompt 发送与结果解析。
 */
public interface ModelGateway {

    /**
     * 调用模型生成文档内容。
     *
     * @param request 经过归一化的生成请求
     * @return 生成结果，若返回 {@link Optional#empty()} 则代表需要走本地兜底
     */
    Optional<ModelGatewayResult> generateDocstring(ModelGatewayRequest request);
}
