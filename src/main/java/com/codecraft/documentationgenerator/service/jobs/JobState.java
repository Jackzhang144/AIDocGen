package com.codecraft.documentationgenerator.service.jobs;

/**
 * 文档生成任务的状态
 */
public enum JobState {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED
}
