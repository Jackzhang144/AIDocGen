package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.ApiKey;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.service.ApiKeyServiceInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 云函数相关接口
 */
@Slf4j
@RestController
@RequestMapping("/functions")
public class FunctionsController {

    private final ApiKeyServiceInterface apiKeyService;
    private final String adminAccessKey;

    public FunctionsController(ApiKeyServiceInterface apiKeyService,
                               @Value("${ADMIN_ACCESS_KEY:}") String adminAccessKey) {
        this.apiKeyService = apiKeyService;
        this.adminAccessKey = adminAccessKey;
    }

    @PostMapping("/typeform")
    public ResponseEntity<Void> typeformWebhook(@RequestBody Map<String, Object> payload) {
        Map<String, Object> formResponse = (Map<String, Object>) payload.get("form_response");
        if (formResponse == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> answers = (List<Map<String, Object>>) formResponse.get("answers");
        if (answers == null) {
            return ResponseEntity.badRequest().build();
        }

        String firstName = extractAnswer(answers, "d566770d2197a78b");
        String lastName = extractAnswer(answers, "88b207c9-e0bc-4128-9159-203abc35b622");
        String email = extractEmail(answers, "4150e35efbf41b8a");
        String purpose = extractAnswer(answers, "d6b6724b54f245f8");

        if (email == null || firstName == null || lastName == null) {
            return ResponseEntity.badRequest().build();
        }

        createApiKey(firstName, lastName, email, purpose);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api")
    public ResponseEntity<Map<String, String>> createApiKey(@RequestBody AdminApiRequest request) {
        if (adminAccessKey == null || adminAccessKey.isEmpty() || !adminAccessKey.equals(request.getAccessKey())) {
            throw new BusinessException("Invalid access key");
        }

        if (request.getFirstName() == null || request.getLastName() == null || request.getEmail() == null) {
            throw new BusinessException("Missing name or email");
        }

        String apiKey = UUID.randomUUID().toString();
        persistApiKey(request.getFirstName(), request.getLastName(), request.getEmail(), null, apiKey);
        return ResponseEntity.ok(Map.of("key", apiKey));
    }

    private void createApiKey(String firstName, String lastName, String email, String purpose) {
        String apiKey = UUID.randomUUID().toString();
        persistApiKey(firstName, lastName, email, purpose, apiKey);
    }

    private void persistApiKey(String firstName, String lastName, String email, String purpose, String apiKey) {
        ApiKey entity = new ApiKey();
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setEmail(email);
        entity.setPurpose(purpose);
        entity.setHashedKey(hashKey(apiKey));
        apiKeyService.createApiKey(entity);
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(key.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm not available", e);
        }
    }

    private String extractAnswer(List<Map<String, Object>> answers, String ref) {
        return answers.stream()
                .filter(answer -> ref.equals(((Map<String, Object>) answer.get("field")).get("ref")))
                .map(answer -> (String) answer.get("text"))
                .filter(value -> value != null && !value.isEmpty())
                .findFirst()
                .orElse(null);
    }

    private String extractEmail(List<Map<String, Object>> answers, String ref) {
        return answers.stream()
                .filter(answer -> ref.equals(((Map<String, Object>) answer.get("field")).get("ref")))
                .map(answer -> (String) answer.get("email"))
                .filter(value -> value != null && !value.isEmpty())
                .findFirst()
                .orElse(null);
    }

    @Data
    public static class AdminApiRequest {
        private String accessKey;
        private String firstName;
        private String lastName;
        private String email;
    }
}
