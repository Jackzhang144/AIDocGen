package com.codecraft.aidoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.codecraft.aidoc.mapper.DocMapper;
import com.codecraft.aidoc.pojo.dto.DocGenerationResult;
import com.codecraft.aidoc.pojo.entity.DocEntity;
import com.codecraft.aidoc.pojo.request.DocGenerationJobRequest;
import com.codecraft.aidoc.service.DocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persists documentation generations for downstream analytics scenarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {

    private final DocMapper docMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordGeneration(DocGenerationJobRequest request, DocGenerationResult result, String feedbackId, long elapsedMs) {
        DocEntity entity = new DocEntity();
        entity.setTimestamp(LocalDateTime.now());
        entity.setUserId(request.getUserId());
        entity.setEmail(request.getEmail());
        entity.setOutput(result.getDocumentation());
        entity.setPrompt(request.getCode() != null ? request.getCode() : request.getContext());
        entity.setLanguage(request.getLanguageId());
        entity.setTimeToGenerate(elapsedMs);
        entity.setTimeToCall(result.getInferenceLatencyMs());
        entity.setSource(request.getSource());
        entity.setFeedbackId(feedbackId);
        entity.setDocFormat(result.getDocFormat() != null ? result.getDocFormat() : request.getDocStyle());
        entity.setCommentFormat(result.getCommentFormat());
        entity.setKind(request.getMode());
        entity.setSelection(Boolean.TRUE.equals(request.getIsSelection()));
        entity.setPromptId(feedbackId);
        entity.setActualLanguage(request.getLanguageId());
        entity.setModelProvider(result.getModelProvider());
        entity.setLatencyMs(result.getInferenceLatencyMs());
        docMapper.insert(entity);
        log.info("[Aidoc] 已保存文档生成记录 user={} feedbackId={}", request.getUserId(), feedbackId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordFeedback(String feedbackId, int score) {
        LambdaUpdateWrapper<DocEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DocEntity::getFeedbackId, feedbackId)
                .set(DocEntity::getFeedback, score);
        docMapper.update(null, wrapper);
        log.info("[Aidoc] 记录文档反馈 feedbackId={} score={}", feedbackId, score);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordMetadata(String feedbackId, String fieldName, String value) {
        LambdaUpdateWrapper<DocEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DocEntity::getFeedbackId, feedbackId);
        switch (fieldName) {
            case "purpose" -> wrapper.set(DocEntity::getKind, value);
            case "source" -> wrapper.set(DocEntity::getSource, value);
            default -> {
                return;
            }
        }
        docMapper.update(null, wrapper);
        log.debug("[Aidoc] 更新文档元数据 feedbackId={} {}=>{}", feedbackId, fieldName, value);
    }
}
