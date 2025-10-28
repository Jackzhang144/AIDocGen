package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.exception.GlobalExceptionHandler;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaygroundControllerTest {

    private CodeParsingServiceInterface codeParsingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        codeParsingService = Mockito.mock(CodeParsingServiceInterface.class);
        PlaygroundController controller = new PlaygroundController(codeParsingService, "secret");
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void synopsisModeShouldReturnAnalysis() throws Exception {
        Mockito.when(codeParsingService.getSynopsis(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(new com.codecraft.documentationgenerator.model.Synopsis());

        mockMvc.perform(post("/playground/mints/synopsis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessKey\":\"secret\",\"code\":\"class A{}\",\"languageId\":\"java\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.synopsis").exists());
    }

    @Test
    void invalidAccessKeyShouldBeRejected() throws Exception {
        mockMvc.perform(post("/playground/mints/ast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessKey\":\"wrong\",\"code\":\"class A{}\",\"languageId\":\"java\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid access key"));
    }
}
