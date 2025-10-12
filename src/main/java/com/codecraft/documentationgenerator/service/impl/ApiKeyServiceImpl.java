package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.constant.MessageConstants;
import com.codecraft.documentationgenerator.entity.ApiKey;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.ApiKeyMapper;
import com.codecraft.documentationgenerator.service.ApiKeyServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * API密钥服务实现类
 * <p>
 * 实现API密钥相关的业务逻辑
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Service
public class ApiKeyServiceImpl implements ApiKeyServiceInterface {

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    /**
     * 根据ID查找API密钥
     *
     * @param id API密钥ID
     * @return ApiKey API密钥对象
     */
    public ApiKey findById(Long id) {
        log.info("Finding API key by ID: {}", id);
        ApiKey apiKey = apiKeyMapper.findById(id);
        if (apiKey == null) {
            throw new BusinessException(MessageConstants.API_KEY_NOT_FOUND);
        }
        return apiKey;
    }

    /**
     * 根据哈希值查找API密钥
     *
     * @param hashedKey 哈希后的API密钥
     * @return ApiKey API密钥对象
     */
    public ApiKey findByHashedKey(String hashedKey) {
        log.info("Finding API key by hashed key: {}", hashedKey);
        ApiKey apiKey = apiKeyMapper.findByHashedKey(hashedKey);
        if (apiKey == null) {
            throw new BusinessException(MessageConstants.API_KEY_NOT_FOUND);
        }
        return apiKey;
    }

    /**
     * 创建新的API密钥
     *
     * @param apiKey API密钥对象
     */
    public void createApiKey(ApiKey apiKey) {
        log.info("Creating new API key for email: {}", apiKey.getEmail());
        apiKeyMapper.insert(apiKey);
    }

    /**
     * 根据ID删除API密钥
     *
     * @param id API密钥ID
     */
    public void deleteById(Long id) {
        log.info("Deleting API key with ID: {}", id);
        ApiKey apiKey = apiKeyMapper.findById(id);
        if (apiKey == null) {
            throw new BusinessException(MessageConstants.API_KEY_NOT_FOUND);
        }
        apiKeyMapper.deleteById(id);
    }

    /**
     * 查找所有API密钥
     *
     * @return List<ApiKey> API密钥列表
     */
    public List<ApiKey> findAll() {
        log.info("Finding all API keys");
        return apiKeyMapper.findAll();
    }
}