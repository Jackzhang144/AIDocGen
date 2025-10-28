package com.codecraft.documentationgenerator.service.ai;

import com.codecraft.documentationgenerator.config.AiProviderProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * DeepSeek API 的调用实现。
 */
@Slf4j
public class DeepSeekModelClient implements AiModelClient {

    private final RestTemplate restTemplate;
    private final AiProviderProperties.DeepSeekProperties properties;

    public DeepSeekModelClient(RestTemplate restTemplate, AiProviderProperties.DeepSeekProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public String generateText(String prompt) {
        Assert.hasText(prompt, "prompt must not be empty");
        Assert.hasText(properties.getApiKey(), "DeepSeek apiKey must be configured");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        DeepSeekChatRequest request = new DeepSeekChatRequest(
                properties.getModel(),
                Collections.singletonList(new DeepSeekMessage("user", prompt)),
                properties.getTemperature()
        );

        HttpEntity<DeepSeekChatRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.postForEntity(
                    "/v1/chat/completions", httpEntity, DeepSeekChatResponse.class);

            DeepSeekChatResponse body = response.getBody();
            if (body == null || body.choices == null || body.choices.isEmpty()) {
                throw new IllegalStateException("DeepSeek API returned empty response");
            }
            DeepSeekChoice firstChoice = body.choices.get(0);
            if (firstChoice.message == null || !StringUtils.hasText(firstChoice.message.content)) {
                throw new IllegalStateException("DeepSeek API returned empty message content");
            }
            return firstChoice.message.content.trim();
        } catch (HttpStatusCodeException httpException) {
            String errorBody = httpException.getResponseBodyAsString();
            log.error("DeepSeek API error (status={}): {}", httpException.getStatusCode(), errorBody);
            throw new IllegalStateException(
                    "DeepSeek API call failed: " + httpException.getStatusCode().value(), httpException);
        } catch (Exception ex) {
            log.error("DeepSeek API invocation error", ex);
            throw new IllegalStateException("Failed to call DeepSeek API", ex);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DeepSeekChatRequest {
        private String model;
        private List<DeepSeekMessage> messages;
        private Double temperature;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DeepSeekMessage {
        private String role;
        private String content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DeepSeekChatResponse {
        private List<DeepSeekChoice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DeepSeekChoice {
        private DeepSeekMessage message;
    }
}
