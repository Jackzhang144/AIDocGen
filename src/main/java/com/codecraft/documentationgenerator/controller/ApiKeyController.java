package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.ApiKey;
import com.codecraft.documentationgenerator.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apikeys")
public class ApiKeyController {
    
    @Autowired
    private ApiKeyService apiKeyService;
    
    @GetMapping("/{id}")
    public ApiKey getApiKeyById(@PathVariable Long id) {
        return apiKeyService.findById(id);
    }
    
    @GetMapping("/hashed/{hashedKey}")
    public ApiKey getApiKeyByHashedKey(@PathVariable String hashedKey) {
        return apiKeyService.findByHashedKey(hashedKey);
    }
    
    @PostMapping
    public void createApiKey(@RequestBody ApiKey apiKey) {
        apiKeyService.createApiKey(apiKey);
    }
    
    @DeleteMapping("/{id}")
    public void deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteById(id);
    }
    
    @GetMapping
    public List<ApiKey> getAllApiKeys() {
        return apiKeyService.findAll();
    }
}