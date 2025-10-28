package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.constant.MessageConstants;
import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.model.GenerateDocRequest;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import com.codecraft.documentationgenerator.service.DocServiceInterface;
import com.codecraft.documentationgenerator.service.impl.DocJobService;
import com.codecraft.documentationgenerator.service.jobs.DocJob;
import com.codecraft.documentationgenerator.service.jobs.JobState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 对应原Mintlify后端的文档生成接口
 */
@Slf4j
@RestController
@RequestMapping("/docs")
public class DocsController {

    private final DocJobService docJobService;
    private final CodeParsingServiceInterface codeParsingService;
    private final DocServiceInterface docService;

    public DocsController(DocJobService docJobService,
                          CodeParsingServiceInterface codeParsingService,
                          DocServiceInterface docService) {
        this.docJobService = docJobService;
        this.codeParsingService = codeParsingService;
        this.docService = docService;
    }

    @PostMapping("/write/v3")
    public ResponseEntity<Map<String, String>> writeDoc(@RequestBody GenerateDocRequest request) {
        validateCodePresence(request);
        request.setIsSelection(true);
        String jobId = docJobService.submitJob(request);
        return ResponseEntity.ok(Map.of("id", jobId));
    }

    @PostMapping("/write/v3/no-selection")
    public ResponseEntity<Map<String, String>> writeDocWithoutSelection(@RequestBody GenerateDocRequest request) {
        String derivedCode = codeParsingService.getCode(
                request.getContext(),
                request.getLanguageId(),
                request.getLocation(),
                request.getLine());
        request.setCode(derivedCode);
        validateCodePresence(request);
        request.setIsSelection(false);
        String jobId = docJobService.submitJob(request);
        return ResponseEntity.ok(Map.of("id", jobId));
    }

    @GetMapping("/worker/{id}")
    public ResponseEntity<?> getWorkerStatus(@PathVariable String id) {
        Optional<DocJob> jobOptional = docJobService.getJob(id);
        if (jobOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        DocJob job = jobOptional.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", job.getId());
        response.put("state", mapState(job.getState()));
        if (job.getReason() != null) {
            response.put("reason", job.getReason());
        }
        if (job.getData() != null) {
            response.put("data", job.getData());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(@RequestBody FeedbackRequest request) {
        Doc doc = docService.findByFeedbackId(request.getId());
        doc.setFeedback(request.getFeedback());
        docService.updateFeedback(doc);
        log.info("Received feedback {} for document {}", request.getFeedback(), request.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/intro")
    public ResponseEntity<Void> submitIntro(@RequestBody IntroRequest request) {
        log.info("Intro survey submission: {}", request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/intro/discover")
    public ResponseEntity<Void> submitIntroDiscover(@RequestBody IntroDiscoverRequest request) {
        log.info("Intro discover submission: {}", request);
        return ResponseEntity.ok().build();
    }

    private void validateCodePresence(GenerateDocRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(MessageConstants.CODE_REQUIRED);
        }
    }

    private String mapState(JobState state) {
        if (state == null) {
            return "unknown";
        }
        switch (state) {
            case QUEUED:
                return "queued";
            case RUNNING:
                return "active";
            case COMPLETED:
                return "completed";
            case FAILED:
                return "failed";
            default:
                return state.name().toLowerCase();
        }
    }

    @Data
    private static class FeedbackRequest {
        private String id;
        private Integer feedback;
    }

    @Data
    private static class IntroRequest {
        private String id;
        private String purpose;
    }

    @Data
    private static class IntroDiscoverRequest {
        private String id;
        private String source;
    }
}
