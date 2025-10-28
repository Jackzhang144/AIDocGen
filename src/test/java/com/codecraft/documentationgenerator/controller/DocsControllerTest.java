package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import com.codecraft.documentationgenerator.service.DocServiceInterface;
import com.codecraft.documentationgenerator.service.impl.DocJobService;
import com.codecraft.documentationgenerator.service.jobs.DocJob;
import com.codecraft.documentationgenerator.service.jobs.DocJobResult;
import com.codecraft.documentationgenerator.service.jobs.JobState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocsControllerTest {

    private DocJobService docJobService;
    private CodeParsingServiceInterface codeParsingService;
    private DocServiceInterface docService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        docJobService = mock(DocJobService.class);
        codeParsingService = mock(CodeParsingServiceInterface.class);
        docService = mock(DocServiceInterface.class);

        DocsController controller = new DocsController(docJobService, codeParsingService, docService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void writeDoc_shouldReturnJobId() throws Exception {
        when(docJobService.submitJob(any())).thenReturn("job-1");

        mockMvc.perform(post("/docs/write/v3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"code\",\"languageId\":\"java\",\"userId\":\"u\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("job-1")));

        verify(docJobService).submitJob(any());
    }

    @Test
    void writeDocWithoutSelection_shouldDeriveCode() throws Exception {
        when(codeParsingService.getCode(nullable(String.class), anyString(), any(), nullable(String.class)))
                .thenReturn("derived");
        when(docJobService.submitJob(any())).thenReturn("job-2");

        mockMvc.perform(post("/docs/write/v3/no-selection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageId\":\"java\",\"userId\":\"u\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("job-2")));

        verify(codeParsingService).getCode(nullable(String.class), eq("java"), any(), nullable(String.class));
    }

    @Test
    void worker_shouldReturnNotFoundWhenJobMissing() throws Exception {
        when(docJobService.getJob("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/docs/worker/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void worker_shouldReturnJobStatusWhenPresent() throws Exception {
        DocJob job = new DocJob("job-3");
        DocJobResult result = new DocJobResult();
        result.setDocstring("content");
        job.markCompleted(result);
        when(docJobService.getJob("job-3")).thenReturn(Optional.of(job));

        mockMvc.perform(get("/docs/worker/job-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("job-3")))
                .andExpect(jsonPath("$.state", is("completed")))
                .andExpect(jsonPath("$.data.docstring", is("content")));
    }

    @Test
    void feedback_shouldUpdateDoc() throws Exception {
        Doc doc = new Doc();
        doc.setId(10L);
        when(docService.findByFeedbackId("foo")).thenReturn(doc);

        mockMvc.perform(post("/docs/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"foo\",\"feedback\":1}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Doc> captor = ArgumentCaptor.forClass(Doc.class);
        verify(docService).updateFeedback(captor.capture());
        Doc updated = captor.getValue();
        assertThat(updated.getFeedback()).isEqualTo(1);
    }
}
