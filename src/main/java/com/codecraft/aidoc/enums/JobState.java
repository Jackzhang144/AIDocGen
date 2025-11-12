package com.codecraft.aidoc.enums;

/**
 * Represents the lifecycle states used by the asynchronous documentation generation jobs.
 */
public enum JobState {
    /**
     * Job accepted but not yet processed.
     */
    PENDING,

    /**
     * Job currently being executed by a worker thread.
     */
    IN_PROGRESS,

    /**
     * Job processed successfully and a result is available.
     */
    SUCCEEDED,

    /**
     * Job failed to process - inspect diagnostic details for the root cause.
     */
    FAILED
}
