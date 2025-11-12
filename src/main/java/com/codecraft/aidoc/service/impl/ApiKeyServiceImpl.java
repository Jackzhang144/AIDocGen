package com.codecraft.aidoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codecraft.aidoc.mapper.ApiKeyMapper;
import com.codecraft.aidoc.pojo.entity.ApiKeyEntity;
import com.codecraft.aidoc.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation that persists API keys using MyBatis-Plus.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyMapper apiKeyMapper;

    @Override
    public Optional<ApiKeyEntity> findByRawKey(String rawKey) {
        final String hashedKey = DigestUtils.sha1Hex(rawKey.trim());
        LambdaQueryWrapper<ApiKeyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiKeyEntity::getHashedKey, hashedKey);
        ApiKeyEntity entity = apiKeyMapper.selectOne(wrapper);
        return Optional.ofNullable(entity);
    }

    @Override
    public List<ApiKeyEntity> listAll() {
        return apiKeyMapper.selectList(null);
    }

    @Override
    public ApiKeyEntity saveKey(String firstName, String lastName, String email, String purpose, String rawKey) {
        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setEmail(email);
        entity.setPurpose(purpose);
        entity.setHashedKey(DigestUtils.sha1Hex(rawKey.trim()));
        entity.setCreatedAt(LocalDateTime.now());
        apiKeyMapper.insert(entity);
        return entity;
    }

    @Override
    public void deleteKey(Long id) {
        apiKeyMapper.deleteById(id);
    }
}
