package com.codecraft.aidoc.service;

import com.codecraft.aidoc.pojo.dto.DocGenerationResult;
import com.codecraft.aidoc.pojo.request.DocGenerationJobRequest;

/**
 * 负责持久化文档生成记录，并提供反馈、调研信息的更新入口。
 */
public interface DocService {

    /**
     * 持久化一次文档生成任务，便于后续做反馈追踪与效果评估。
     *
     * @param request   原始任务请求
     * @param result    生成结果
     * @param feedbackId 反馈标识
     * @param elapsedMs  任务耗时（毫秒）
     */
    void recordGeneration(DocGenerationJobRequest request, DocGenerationResult result, String feedbackId, long elapsedMs);

    /**
     * 写入用户反馈分值。
     *
     * @param feedbackId 反馈标识
     * @param score      打分
     */
    void recordFeedback(String feedbackId, int score);

    /**
     * 保存调研类元数据（purpose/source）。
     *
     * @param feedbackId 反馈标识
     * @param fieldName  字段
     * @param value      值
     */
    void recordMetadata(String feedbackId, String fieldName, String value);
}
