package com.codecraft;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekApiKeyTest {

    @Test
    void apiKeyShouldBePresentAndWellFormedWhenConfigured() {
        String key = System.getenv("DEEPSEEK_API_KEY");
        Assumptions.assumeTrue(key != null && !key.isBlank(), "未配置 DEEPSEEK_API_KEY，跳过此校验");

        // 基础格式校验：DeepSeek key 通常以 sk- 开头且长度较长
        assertThat(key).startsWith("sk-");
        assertThat(key.length()).isGreaterThan(20);
    }
}
