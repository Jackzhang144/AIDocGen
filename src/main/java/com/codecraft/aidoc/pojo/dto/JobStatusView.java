package com.codecraft.aidoc.pojo.dto;

import com.codecraft.aidoc.enums.JobState;
import lombok.Builder;
import lombok.Data;

/**
 * DTO surfaced to consumers polling asynchronous documentation generation jobs.
 */
@Data
@Builder
public class JobStatusView {

    private String jobId;

    private JobState state;

    private String reason;

    private DocGenerationResult result;
}
