package com.codecraft.aidoc.service;

import com.codecraft.aidoc.pojo.dto.JobStatusView;
import com.codecraft.aidoc.pojo.request.DocGenerationJobRequest;

/**
 * Manages asynchronous documentation generation jobs, allowing clients to enqueue work and
 * poll for completion.
 */
public interface DocJobService {

    /**
     * Submits a documentation generation job that operates on a user selection.
     *
     * @param request job payload
     * @return generated job identifier
     */
    String submitSelectionJob(Long userId, DocGenerationJobRequest request);

    /**
     * Submits a job that first derives the selection before generating documentation.
     *
     * @param request job payload
     * @return generated job identifier
     */
    String submitContextualJob(Long userId, DocGenerationJobRequest request);

    /**
     * Retrieves the job status for polling clients.
     *
     * @param jobId identifier returned during submission
     * @return job view with state and optional result
     */
    JobStatusView getJob(Long userId, String jobId);

    /**
     * Records user feedback for a previously generated documentation sample.
     *
     * @param feedbackId    feedback identifier
     * @param feedback rating (-1, 0, 1)
     */
    void recordFeedback(String feedbackId, int feedback);

    /**
     * Stores onboarding survey metadata.
     *
     * @param jobId   identifier
     * @param purpose usage purpose
     */
    void recordIntro(String jobId, String purpose);

    /**
     * Records discovery channel information.
     *
     * @param jobId identifier
     * @param source marketing channel
     */
    void recordIntroDiscover(String jobId, String source);
}
