package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.ApiKey;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.ApiKeyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ApiKeyServiceImplTest {

    @Mock
    private ApiKeyMapper apiKeyMapper;

    @InjectMocks
    private ApiKeyServiceImpl apiKeyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_ApiKeyExists_ReturnsApiKey() {
        // Given
        ApiKey expectedApiKey = new ApiKey();
        expectedApiKey.setId(1L);
        expectedApiKey.setEmail("user@example.com");
        when(apiKeyMapper.findById(1L)).thenReturn(expectedApiKey);

        // When
        ApiKey actualApiKey = apiKeyService.findById(1L);

        // Then
        assertNotNull(actualApiKey);
        assertEquals(expectedApiKey.getId(), actualApiKey.getId());
        assertEquals(expectedApiKey.getEmail(), actualApiKey.getEmail());
        verify(apiKeyMapper).findById(1L);
    }

    @Test
    void findById_ApiKeyNotExists_ThrowsBusinessException() {
        // Given
        when(apiKeyMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> apiKeyService.findById(1L));
        assertEquals("API密钥不存在", exception.getMessage());
        verify(apiKeyMapper).findById(1L);
    }

    @Test
    void findByHashedKey_ApiKeyExists_ReturnsApiKey() {
        // Given
        ApiKey expectedApiKey = new ApiKey();
        expectedApiKey.setId(1L);
        expectedApiKey.setHashedKey("hashed_key");
        when(apiKeyMapper.findByHashedKey("hashed_key")).thenReturn(expectedApiKey);

        // When
        ApiKey actualApiKey = apiKeyService.findByHashedKey("hashed_key");

        // Then
        assertNotNull(actualApiKey);
        assertEquals(expectedApiKey.getHashedKey(), actualApiKey.getHashedKey());
        verify(apiKeyMapper).findByHashedKey("hashed_key");
    }

    @Test
    void findByHashedKey_ApiKeyNotExists_ThrowsBusinessException() {
        // Given
        when(apiKeyMapper.findByHashedKey("hashed_key")).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> apiKeyService.findByHashedKey("hashed_key"));
        assertEquals("API密钥不存在", exception.getMessage());
        verify(apiKeyMapper).findByHashedKey("hashed_key");
    }

    @Test
    void createApiKey_ValidApiKey_CreatesApiKey() {
        // Given
        ApiKey apiKey = new ApiKey();
        apiKey.setEmail("user@example.com");

        // When
        apiKeyService.createApiKey(apiKey);

        // Then
        verify(apiKeyMapper).insert(any(ApiKey.class));
    }

    @Test
    void deleteById_ApiKeyExists_DeletesApiKey() {
        // Given
        ApiKey apiKey = new ApiKey();
        apiKey.setId(1L);
        when(apiKeyMapper.findById(1L)).thenReturn(apiKey);

        // When
        apiKeyService.deleteById(1L);

        // Then
        verify(apiKeyMapper).deleteById(1L);
    }

    @Test
    void deleteById_ApiKeyNotExists_ThrowsBusinessException() {
        // Given
        when(apiKeyMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> apiKeyService.deleteById(1L));
        assertEquals("API密钥不存在", exception.getMessage());
        verify(apiKeyMapper, never()).deleteById(anyLong());
    }

    @Test
    void findAll_ReturnsAllApiKeys() {
        // Given
        ApiKey apiKey1 = new ApiKey();
        apiKey1.setId(1L);
        apiKey1.setEmail("user1@example.com");
        
        ApiKey apiKey2 = new ApiKey();
        apiKey2.setId(2L);
        apiKey2.setEmail("user2@example.com");
        
        List<ApiKey> expectedApiKeys = Arrays.asList(apiKey1, apiKey2);
        when(apiKeyMapper.findAll()).thenReturn(expectedApiKeys);

        // When
        List<ApiKey> actualApiKeys = apiKeyService.findAll();

        // Then
        assertNotNull(actualApiKeys);
        assertEquals(2, actualApiKeys.size());
        assertEquals(expectedApiKeys, actualApiKeys);
        verify(apiKeyMapper).findAll();
    }
}