package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.ApiKey;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.exception.GlobalExceptionHandler;
import com.codecraft.documentationgenerator.exception.UnauthorizedException;
import com.codecraft.documentationgenerator.service.ApiKeyServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicApiControllerTest {

    private ApiKeyServiceInterface apiKeyService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        apiKeyService = Mockito.mock(ApiKeyServiceInterface.class);
        PublicApiController controller = new PublicApiController(apiKeyService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listLanguages_shouldRequireApiKey() throws Exception {
        mockMvc.perform(get("/v1/list/languages"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No API key provided"));
    }

    @Test
    void listLanguages_shouldReturnAvailableOptions() throws Exception {
        String plainKey = "demo-key";
        Mockito.when(apiKeyService.findByHashedKey(hash(plainKey))).thenReturn(new ApiKey());

        mockMvc.perform(get("/v1/list/languages")
                        .header("API-KEY", plainKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.languages").isArray());
    }

    @Test
    void document_shouldBubbleUpMaintenanceMessage() throws Exception {
        String plainKey = "demo-key";
        Mockito.when(apiKeyService.findByHashedKey(hash(plainKey))).thenReturn(new ApiKey());

        mockMvc.perform(post("/v1/document")
                        .header("API-KEY", plainKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"print('hi')\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The Mintlify API is currently being updated. Please email hi@mintlify for urgent authorization"));
    }

    @Test
    void invalidKeyShouldReturnUnauthorized() throws Exception {
        String plainKey = "invalid";
        Mockito.when(apiKeyService.findByHashedKey(hash(plainKey))).thenThrow(new BusinessException("API密钥不存在"));

        mockMvc.perform(get("/v1/list/formats").header("API-KEY", plainKey))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid API key"));
    }

    private String hash(String plainText) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = digest.digest(plainText.trim().getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
