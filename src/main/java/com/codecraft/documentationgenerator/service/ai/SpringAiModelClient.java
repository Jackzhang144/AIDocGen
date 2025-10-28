package com.codecraft.documentationgenerator.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

/**
 * 基于 Spring AI {@link ChatClient} 的模型调用实现。
 */
@RequiredArgsConstructor
public class SpringAiModelClient implements AiModelClient {

    private final ChatClient chatClient;

    @Override
    public String generateText(String prompt) {
        Assert.hasText(prompt, "prompt must not be empty");
        return chatClient.prompt(new Prompt(prompt)).call().content();
    }
}
