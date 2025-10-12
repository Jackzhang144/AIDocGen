package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.aop.RequireLogin;
import com.codecraft.documentationgenerator.entity.ApiKey;
import com.codecraft.documentationgenerator.service.ApiKeyServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API密钥控制器
 * <p>
 * 处理API密钥相关的RESTful API请求
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/apikeys")
public class ApiKeyController {

    @Autowired
    private ApiKeyServiceInterface apiKeyService;

    /**
     * 根据ID获取API密钥
     *
     * @param id API密钥ID
     * @return ApiKey API密钥对象
     */
    @RequireLogin
    @GetMapping("/{id}")
    public ApiKey getApiKeyById(@PathVariable Long id) {
        log.info("Fetching API key by ID: {}", id);
        return apiKeyService.findById(id);
    }

    /**
     * 根据哈希值获取API密钥
     *
     * @param hashedKey 哈希后的API密钥
     * @return ApiKey API密钥对象
     */
    @RequireLogin
    @GetMapping("/hashed/{hashedKey}")
    public ApiKey getApiKeyByHashedKey(@PathVariable String hashedKey) {
        log.info("Fetching API key by hashed key: {}", hashedKey);
        return apiKeyService.findByHashedKey(hashedKey);
    }

    /**
     * 创建新的API密钥
     *
     * @param apiKey API密钥对象
     */
    @RequireLogin
    @PostMapping
    public void createApiKey(@RequestBody ApiKey apiKey) {
        log.info("Creating new API key for user");
        apiKeyService.createApiKey(apiKey);
    }

    /**
     * 删除指定ID的API密钥
     *
     * @param id API密钥ID
     */
    @RequireLogin
    @DeleteMapping("/{id}")
    public void deleteApiKey(@PathVariable Long id) {
        log.info("Deleting API key with ID: {}", id);
        apiKeyService.deleteById(id);
    }

    /**
     * 获取所有API密钥列表
     *
     * @return List<ApiKey> API密钥列表
     */
    @RequireLogin
    @GetMapping
    public List<ApiKey> getAllApiKeys() {
        log.info("Fetching all API keys");
        return apiKeyService.findAll();
    }
}