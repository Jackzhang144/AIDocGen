package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.entity.ApiKey;

import java.util.List;

public interface ApiKeyService {

    ApiKey findById(Long id);

    ApiKey findByHashedKey(String hashedKey);

    void createApiKey(ApiKey apiKey);

    void deleteById(Long id);

    List<ApiKey> findAll();
}