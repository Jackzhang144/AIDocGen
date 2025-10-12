package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.ApiKey;
import com.codecraft.documentationgenerator.mapper.ApiKeyMapper;
import com.codecraft.documentationgenerator.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    public ApiKey findById(Long id) {
        return apiKeyMapper.findById(id);
    }

    public ApiKey findByHashedKey(String hashedKey) {
        return apiKeyMapper.findByHashedKey(hashedKey);
    }

    public void createApiKey(ApiKey apiKey) {
        apiKeyMapper.insert(apiKey);
    }

    public void deleteById(Long id) {
        apiKeyMapper.deleteById(id);
    }

    public List<ApiKey> findAll() {
        return apiKeyMapper.findAll();
    }
}