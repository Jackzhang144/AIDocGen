package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.RequiresAuthenticationException;
import com.codecraft.documentationgenerator.model.GenerateDocRequest;
import com.codecraft.documentationgenerator.model.Synopsis;
import com.codecraft.documentationgenerator.service.AiDocumentationServiceInterface;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import com.codecraft.documentationgenerator.service.DocServiceInterface;
import com.codecraft.documentationgenerator.service.TeamServiceInterface;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import com.codecraft.documentationgenerator.service.jobs.DocJob;
import com.codecraft.documentationgenerator.service.jobs.JobState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocJobServiceTest {

    @Mock
    private AiDocumentationServiceInterface aiDocumentationService;
    @Mock
    private CodeParsingServiceInterface codeParsingService;
    @Mock
    private DocServiceInterface docService;
    @Mock
    private UserServiceInterface userService;
    @Mock
    private TeamServiceInterface teamService;

    private ExecutorService executor;
    private DocJobService docJobService;

    @BeforeEach
    void setUp() {
        executor = Executors.newSingleThreadExecutor();
        docJobService = new DocJobService(aiDocumentationService, codeParsingService, docService, userService, teamService, executor);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void submitJob_shouldGenerateDocumentationAndPersist() throws Exception {
        GenerateDocRequest request = buildRequest(true);

        Synopsis synopsis = new Synopsis();
        synopsis.setKind("function");

        when(codeParsingService.getSynopsis(anyString(), anyString(), anyString())).thenReturn(synopsis);
        when(aiDocumentationService.generateFunctionDocstring(anyString(), eq(synopsis), anyString()))
                .thenReturn("Adds two numbers");
        when(docService.countDocsByUserSince(anyString(), any())).thenReturn(0);
        when(userService.findByEmailOrNull("dev@example.com")).thenReturn(new User());
        when(teamService.findByEmail("dev@example.com")).thenReturn(null);
        when(docService.hasPositiveFeedback("user-1")).thenReturn(false);
        when(docService.findRecentDocs("user-1", 3)).thenReturn(Collections.emptyList());
        User stored = new User();
        stored.setId(1L);
        stored.setUpdatedAt(LocalDateTime.now());
        when(userService.findByUserUid("user-1")).thenReturn(stored);

        ArgumentCaptor<Doc> docCaptor = ArgumentCaptor.forClass(Doc.class);
        doNothing().when(docService).createDoc(docCaptor.capture());

        String jobId = docJobService.submitJob(request);
        DocJob job = awaitJobCompletion(jobId);

        assertThat(job.getState()).isEqualTo(JobState.COMPLETED);
        assertThat(job.getData().getDocstring()).contains("/**").contains("Adds two numbers");
        assertThat(job.getData().getPosition()).isEqualTo("Above");
        assertThat(job.getData().getFeedbackId()).isNotBlank();

        Doc saved = docCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getLanguage()).isEqualTo("javascript");
        assertThat(saved.getCommentFormat()).isEqualTo("JSDoc");
        assertThat(saved.getOutput()).contains("Adds two numbers");

        verify(docService).createDoc(any(Doc.class));
        verify(userService).updateLastActive(stored);
    }

    @Test
    void submitJob_shouldEnforceQuotaForAnonymousUsers() {
        GenerateDocRequest request = buildRequest(false);
        when(docService.countDocsByUserSince(anyString(), any())).thenReturn(120);
        when(userService.findByEmailOrNull(anyString())).thenReturn(null);
        when(teamService.findByEmail(anyString())).thenReturn(null);

        assertThatThrownBy(() -> docJobService.submitJob(request))
                .isInstanceOf(RequiresAuthenticationException.class)
                .hasMessageContaining("Please sign in to continue");
    }

    @Test
    void submitJob_shouldSkipQuotaWhenBelongsToTeam() throws InterruptedException {
        GenerateDocRequest request = buildRequest(false);
        when(docService.countDocsByUserSince(anyString(), any())).thenReturn(120);
        when(userService.findByEmailOrNull(anyString())).thenReturn(null);
        Team mockTeam = new Team();
        mockTeam.setAdmin("lead@example.com");
        when(teamService.findByEmail("dev@example.com")).thenReturn(mockTeam);
        Synopsis synopsis = new Synopsis();
        synopsis.setKind("function");
        when(codeParsingService.getSynopsis(anyString(), anyString(), anyString())).thenReturn(synopsis);
        when(aiDocumentationService.generateFunctionDocstring(anyString(), eq(synopsis), anyString()))
                .thenReturn("Docstring");
        when(docService.hasPositiveFeedback(anyString())).thenReturn(false);
        when(docService.findRecentDocs(anyString(), anyInt())).thenReturn(Collections.emptyList());
        when(userService.findByUserUid(anyString())).thenReturn(new User());

        String jobId = docJobService.submitJob(request);
        DocJob job = awaitJobCompletion(jobId);

        assertThat(job.getState()).isEqualTo(JobState.COMPLETED);
    }

    private GenerateDocRequest buildRequest(boolean commented) {
        GenerateDocRequest request = new GenerateDocRequest();
        request.setCode("function add(a, b) { return a + b; }");
        request.setLanguageId("javascript");
        request.setUserId("user-1");
        request.setEmail("dev@example.com");
        request.setContext("function add(a, b) { return a + b; }");
        request.setSource("vscode");
        request.setCommented(commented);
        request.setWidth(60);
        return request;
    }

    private DocJob awaitJobCompletion(String jobId) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            Optional<DocJob> maybeJob = docJobService.getJob(jobId);
            if (maybeJob.isPresent() && maybeJob.get().getState() == JobState.COMPLETED) {
                return maybeJob.get();
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
        throw new AssertionError("Job did not complete in expected time");
    }
}
