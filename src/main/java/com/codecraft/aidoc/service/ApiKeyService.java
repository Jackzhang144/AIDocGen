package com.codecraft.aidoc.service;

import com.codecraft.aidoc.pojo.entity.ApiKeyEntity;

import java.util.List;
import java.util.Optional;

/**
 * Handles validation lookups for API keys used by the public Mintlify-compatible API.
 */
public interface ApiKeyService {

    /**
     * Validates the supplied raw API key by hashing it and verifying its existence in the data store.
     *
     * @param rawKey api key received from the client
     * @return existing API key entity when the key is valid
     */
    Optional<ApiKeyEntity> findByRawKey(String rawKey);

    List<ApiKeyEntity> listAll();

    ApiKeyEntity saveKey(String firstName, String lastName, String email, String purpose, String rawKey);

    void deleteKey(Long id);
}
