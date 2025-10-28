package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.exception.UnauthorizedException;
import com.codecraft.documentationgenerator.service.ApiKeyServiceInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 公共 API 接口
 */
@Slf4j
@RestController
@RequestMapping("/v1")
public class PublicApiController {

    private final ApiKeyServiceInterface apiKeyService;

    public PublicApiController(ApiKeyServiceInterface apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/document")
    public ResponseEntity<Map<String, String>> generateDocument(@RequestHeader(value = "API-KEY", required = false) String apiKey,
                                                                @RequestBody(required = false) Map<String, Object> body) {
        log.info("Public API document generation requested (payloadSize={})",
                body == null ? 0 : body.size());
        validateApiKey(apiKey);
        return ResponseEntity.badRequest().body(Map.of(
                "error", "The Mintlify API is currently being updated. Please email hi@mintlify for urgent authorization"
        ));
    }

    @GetMapping("/list/languages")
    public ResponseEntity<Map<String, List<String>>> listLanguages(@RequestHeader(value = "API-KEY", required = false) String apiKey) {
        log.info("Listing supported languages via public API");
        validateApiKey(apiKey);
        List<String> languages = List.of("python", "javascript", "typescript", "javascriptreact", "typescriptreact", "php", "c", "cpp");
        return ResponseEntity.ok(Map.of("languages", languages));
    }

    @GetMapping("/list/formats")
    public ResponseEntity<Map<String, List<FormatInfo>>> listFormats(@RequestHeader(value = "API-KEY", required = false) String apiKey) {
        log.info("Listing supported formats via public API");
        validateApiKey(apiKey);
        List<FormatInfo> formats = List.of(
                new FormatInfo("JSDoc", List.of("javascript", "typescript", "javascriptreact", "typescriptreact")),
                new FormatInfo("ReST", List.of("python")),
                new FormatInfo("DocBlock", List.of("php", "c", "cpp")),
                new FormatInfo("Google", List.of())
        );
        return ResponseEntity.ok(Map.of("formats", formats));
    }

    private void validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Public API access denied: missing API key");
            throw new UnauthorizedException("No API key provided");
        }
        log.debug("Validating API key {}", maskKey(apiKey));
        String hashedKey = hashKey(apiKey);
        try {
            apiKeyService.findByHashedKey(hashedKey);
        } catch (BusinessException ex) {
            log.warn("Public API access denied: invalid API key {}", maskKey(apiKey));
            throw new UnauthorizedException("Invalid API key");
        }
        log.debug("API key {} validated successfully", maskKey(apiKey));
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

    private String maskKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "<empty>";
        }
        String trimmed = apiKey.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return trimmed.substring(0, 4) + "****";
    }

    @Data
    public static class FormatInfo {
        private final String id;
        private final List<String> defaultLanguages;
    }
}
