package com.codecraft.documentationgenerator.exception;

import com.codecraft.documentationgenerator.DocumentationGeneratorApplication;
import com.codecraft.documentationgenerator.controller.UserController;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(DocumentationGeneratorApplication.class)
public class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceInterface userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBusinessExceptionHandling() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.findById(userId)).thenThrow(new BusinessException("用户不存在"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户不存在"))
                .andExpect(jsonPath("$.data").value(null));
    }

    @Test
    public void testGeneralExceptionHandling() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.findById(userId)).thenThrow(new RuntimeException("系统异常"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("服务器异常"))
                .andExpect(jsonPath("$.data").value(null));
    }
}