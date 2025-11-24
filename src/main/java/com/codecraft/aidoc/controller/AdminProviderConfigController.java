package com.codecraft.aidoc.controller;

import com.codecraft.aidoc.common.ApiResponse;
import com.codecraft.aidoc.pojo.entity.AiProviderConfigEntity;
import com.codecraft.aidoc.service.AiProviderConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台：配置与切换 AI 模型提供方的密钥与连接信息。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/providers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProviderConfigController {

    private final AiProviderConfigService configService;

    @GetMapping
    public ApiResponse<List<AiProviderConfigEntity>> list() {
        return ApiResponse.ok("获取提供方配置成功", configService.listAll());
    }

    @PostMapping
    public ApiResponse<AiProviderConfigEntity> save(@Valid @RequestBody AiProviderConfigEntity entity) {
        AiProviderConfigEntity saved = configService.saveOrUpdate(entity);
        return ApiResponse.ok("保存成功", saved);
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<Map<String, Long>> activate(@PathVariable Long id) {
        configService.activate(id);
        return ApiResponse.ok("已切换提供方", Map.of("id", id));
    }
}
