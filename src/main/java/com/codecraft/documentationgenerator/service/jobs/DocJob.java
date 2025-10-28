package com.codecraft.documentationgenerator.service.jobs;

import lombok.Data;

import java.time.Instant;

/**
 * 文档生成任务描述
 */
@Data
public class DocJob {
    private final String id;
    private volatile JobState state;
    private volatile String reason;
    private volatile DocJobResult data;
    private final Instant createdAt;
    private volatile Instant updatedAt;

    public DocJob(String id) {
        this.id = id;
        this.state = JobState.QUEUED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void markRunning() {
        this.state = JobState.RUNNING;
        this.updatedAt = Instant.now();
    }

    public void markCompleted(DocJobResult result) {
        this.state = JobState.COMPLETED;
        this.data = result;
        this.reason = null;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.state = JobState.FAILED;
        this.reason = reason;
        this.updatedAt = Instant.now();
    }
}
