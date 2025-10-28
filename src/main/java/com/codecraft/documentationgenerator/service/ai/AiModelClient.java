package com.codecraft.documentationgenerator.service.ai;

/**
 * 抽象化的通用AI文本生成客户端。
 * <p>
 * 通过统一的接口屏蔽具体模型供应商的差异。
 */
public interface AiModelClient {

    /**
     * 使用底层模型根据提示词获取文本响应。
     *
     * @param prompt 完整提示词
     * @return 模型返回的文本内容
     */
    String generateText(String prompt);
}
