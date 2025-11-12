package com.codecraft.aidoc.controller;

import com.codecraft.aidoc.common.ApiResponse;
import com.codecraft.aidoc.pojo.entity.ApiKeyEntity;
import com.codecraft.aidoc.pojo.entity.UserEntity;
import com.codecraft.aidoc.pojo.request.CreateApiKeyRequest;
import com.codecraft.aidoc.pojo.request.UpdateUserRequest;
import com.codecraft.aidoc.pojo.response.ApiKeyResponse;
import com.codecraft.aidoc.pojo.response.UserSummaryResponse;
import com.codecraft.aidoc.service.ApiKeyService;
import com.codecraft.aidoc.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin endpoints for managing users and API keys.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ApiKeyService apiKeyService;

    @GetMapping("/users")
    public ApiResponse<List<UserSummaryResponse>> listUsers() {
        List<UserSummaryResponse> payload = userService.listUsers().stream()
                .map(user -> UserSummaryResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .apiQuota(user.getApiQuota())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.ok("获取用户列表成功", payload);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserSummaryResponse> updateUser(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateUserRequest request) {
        UserEntity entity = userService.updateUser(id, request.getEmail(), request.getRole(), request.getApiQuota());
        UserSummaryResponse response = UserSummaryResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .apiQuota(entity.getApiQuota())
                .createdAt(entity.getCreatedAt())
                .build();
        return ApiResponse.ok("更新用户成功", response);
    }

    @GetMapping("/api-keys")
    public ApiResponse<List<ApiKeyResponse>> listApiKeys() {
        List<ApiKeyResponse> keys = apiKeyService.listAll().stream()
                .map(entity -> ApiKeyResponse.builder()
                        .id(entity.getId())
                        .email(entity.getEmail())
                        .firstName(entity.getFirstName())
                        .lastName(entity.getLastName())
                        .purpose(entity.getPurpose())
                        .createdAt(entity.getCreatedAt())
                        .hashedKey(entity.getHashedKey())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.ok("获取 API Key 成功", keys);
    }

    @PostMapping("/api-keys")
    public ApiResponse<ApiKeyResponse> createApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        ApiKeyEntity entity = apiKeyService.saveKey(request.getFirstName(), request.getLastName(),
                request.getEmail(), request.getPurpose(), request.getRawKey());
        ApiKeyResponse response = ApiKeyResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .purpose(entity.getPurpose())
                .createdAt(entity.getCreatedAt())
                .hashedKey(entity.getHashedKey())
                .rawKey(request.getRawKey())
                .build();
        return ApiResponse.ok("创建 API Key 成功", response);
    }

    @DeleteMapping("/api-keys/{id}")
    public ApiResponse<Map<String, Long>> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteKey(id);
        return ApiResponse.ok("删除 API Key 成功", Map.of("id", id));
    }
}
