package com.codecraft.aidoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codecraft.aidoc.enums.ErrorCode;
import com.codecraft.aidoc.enums.JobState;
import com.codecraft.aidoc.exception.BusinessException;
import com.codecraft.aidoc.mapper.DocJobMapper;
import com.codecraft.aidoc.pojo.dto.DocGenerationResult;
import com.codecraft.aidoc.pojo.dto.JobStatusView;
import com.codecraft.aidoc.pojo.entity.DocJobEntity;
import com.codecraft.aidoc.pojo.request.DocGenerationJobRequest;
import com.codecraft.aidoc.pojo.request.DocumentGenerationRequest;
import com.codecraft.aidoc.service.DocJobService;
import com.codecraft.aidoc.service.DocService;
import com.codecraft.aidoc.service.DocumentationService;
import com.codecraft.aidoc.service.TelemetryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Coordinates background documentation generation with persisted job states so that restarts can resume work.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocJobServiceImpl implements DocJobService {

    private final DocumentationService documentationService;
    private final DocService docService;
    private final TelemetryService telemetryService;
    private final DocJobMapper docJobMapper;
    private final ObjectMapper objectMapper;
    private final @Qualifier("docGenerationExecutor") ThreadPoolTaskExecutor docGenerationExecutor;

    @PostConstruct
    public void configureExecutor() {
        docGenerationExecutor.setThreadNamePrefix("doc-job-");
        recoverPendingJobs();
    }

    @Override
    public String submitSelectionJob(Long userId, DocGenerationJobRequest request) {
        request.setIsSelection(Boolean.TRUE);
        return submitJob(userId, request);
    }

    @Override
    public String submitContextualJob(Long userId, DocGenerationJobRequest request) {
        request.setIsSelection(Boolean.FALSE);
        return submitJob(userId, request);
    }

    private String submitJob(Long userId, DocGenerationJobRequest request) {
        if (!StringUtils.hasText(request.getCode()) && !StringUtils.hasText(request.getContext())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少 code 或 context 字段");
        }
        String jobId = UUID.randomUUID().toString();
        DocJobEntity entity = new DocJobEntity();
        entity.setJobId(jobId);
        entity.setUserId(userId);
        entity.setState(JobState.PENDING);
        entity.setPayload(toJson(request));
        docJobMapper.insert(entity);

        dispatchJob(jobId);
        log.info("[Aidoc] 已入队文档任务 job={} language={}", jobId, request.getLanguageId());
        return jobId;
    }

    private void dispatchJob(String jobId) {
        CompletableFuture.runAsync(() -> processJob(jobId), docGenerationExecutor)
                .exceptionally(throwable -> {
                    log.error("[Aidoc] 文档任务 {} 执行失败", jobId, throwable);
                    markFailure(jobId, throwable.getMessage());
                    return null;
                });
    }

    private void processJob(String jobId) {
        try {
            DocJobEntity entity = docJobMapper.selectById(jobId);
            if (entity == null) {
                log.warn("[Aidoc] 无法找到任务 {}", jobId);
                return;
            }
            if (entity.getState() == JobState.SUCCEEDED) {
                return;
            }
            updateState(jobId, JobState.IN_PROGRESS, null, null);
            DocGenerationJobRequest request = fromJson(entity.getPayload());

            DocumentGenerationRequest docRequest = new DocumentGenerationRequest();
            docRequest.setCode(StringUtils.hasText(request.getCode()) ? request.getCode() : request.getContext());
            docRequest.setLanguage(request.getLanguageId());
            docRequest.setCommented(Boolean.TRUE.equals(request.getCommented()));
            docRequest.setFormat(request.getDocStyle());
            docRequest.setContext(request.getContext());
            docRequest.setWidth(request.getWidth());

            long startedAt = System.currentTimeMillis();
            DocGenerationResult result = documentationService.generateDocumentation(docRequest);
            if (result == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文档生成结果为空");
            }
            long elapsed = System.currentTimeMillis() - startedAt;
            String feedbackId = UUID.randomUUID().toString();

            DocGenerationResult enriched = DocGenerationResult.builder()
                    .documentation(result.getDocumentation())
                    .preview(result.getPreview())
                    .position(result.getPosition())
                    .cursorMarker(result.getCursorMarker())
                    .feedbackId(feedbackId)
                    .docFormat(result.getDocFormat())
                    .commentFormat(result.getCommentFormat())
                    .modelProvider(result.getModelProvider())
                    .inferenceLatencyMs(result.getInferenceLatencyMs())
                    .build();

            docService.recordGeneration(request, enriched, feedbackId, elapsed);
            final String provider = enriched.getModelProvider() != null ? enriched.getModelProvider() : "unknown";
            telemetryService.track(request.getUserId(), "Doc Generated", Map.of(
                    "language", request.getLanguageId(),
                    "elapsedMs", elapsed,
                    "selection", Boolean.TRUE.equals(request.getIsSelection()),
                    "provider", provider
            ));

            updateState(jobId, JobState.SUCCEEDED, null, toJson(enriched));
        } catch (Exception ex) {
            markFailure(jobId, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public JobStatusView getJob(Long userId, String jobId) {
        DocJobEntity entity = docJobMapper.selectById(jobId);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "任务不存在: " + jobId);
        }
        DocGenerationResult result = entity.getResult() != null ? fromResultJson(entity.getResult()) : null;
        return JobStatusView.builder()
                .jobId(jobId)
                .state(entity.getState())
                .reason(entity.getReason())
                .result(result)
                .build();
    }

    @Override
    public void recordFeedback(String feedbackId, int feedback) {
        docService.recordFeedback(feedbackId, feedback);
        telemetryService.track("feedback", "Doc Feedback", Map.of(
                "feedbackId", feedbackId,
                "score", feedback
        ));
    }

    @Override
    public void recordIntro(String jobId, String purpose) {
        docService.recordMetadata(jobId, "purpose", purpose);
    }

    @Override
    public void recordIntroDiscover(String jobId, String source) {
        docService.recordMetadata(jobId, "source", source);
    }

    private void updateState(String jobId, JobState state, String reason, String resultJson) {
        DocJobEntity entity = docJobMapper.selectById(jobId);
        if (entity == null) {
            return;
        }
        entity.setState(state);
        entity.setReason(reason);
        entity.setResult(resultJson);
        docJobMapper.updateById(entity);
    }

    private void markFailure(String jobId, String reason) {
        updateState(jobId, JobState.FAILED, reason, null);
    }

    private void recoverPendingJobs() {
        LambdaQueryWrapper<DocJobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DocJobEntity::getState, JobState.PENDING, JobState.IN_PROGRESS);
        List<DocJobEntity> pending = docJobMapper.selectList(wrapper);
        pending.forEach(job -> dispatchJob(job.getJobId()));
        log.info("[Aidoc] 已恢复 {} 个未完成的文档任务", pending.size());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "序列化任务失败: " + e.getMessage());
        }
    }

    private DocGenerationJobRequest fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, DocGenerationJobRequest.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "反序列化任务失败: " + e.getMessage());
        }
    }

    private DocGenerationResult fromResultJson(String payload) {
        try {
            return objectMapper.readValue(payload, DocGenerationResult.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "读取任务结果失败: " + e.getMessage());
        }
    }
}
