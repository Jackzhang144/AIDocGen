package com.codecraft.aidoc.gateway;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 默认的空实现，当未配置真实模型时直接返回空以触发业务兜底。
 */
@Slf4j
public class NoopModelGateway implements ModelGateway {

    @Override
    public Optional<ModelGatewayResult> generateDocstring(ModelGatewayRequest request) {
        log.debug("[AIDocGen] 模型网关未启用，回退至本地启发式生成");
        return Optional.empty();
    }
}
