package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.entity.ApiKey;

import java.util.List;

/**
 * API密钥服务接口
 * <p>
 * 提供API密钥相关的业务逻辑处理
 *
 * @author CodeCraft
 * @version 1.0
 */
public interface ApiKeyServiceInterface {

    /**
     * 根据ID查找API密钥
     *
     * @param id API密钥ID
     * @return ApiKey API密钥对象
     */
    ApiKey findById(Long id);

    /**
     * 根据哈希值查找API密钥
     *
     * @param hashedKey 哈希后的API密钥
     * @return ApiKey API密钥对象
     */
    ApiKey findByHashedKey(String hashedKey);

    /**
     * 创建新的API密钥
     *
     * @param apiKey API密钥对象
     */
    void createApiKey(ApiKey apiKey);

    /**
     * 根据ID删除API密钥
     *
     * @param id API密钥ID
     */
    void deleteById(Long id);

    /**
     * 查找所有API密钥
     *
     * @return List<ApiKey> API密钥列表
     */
    List<ApiKey> findAll();
}