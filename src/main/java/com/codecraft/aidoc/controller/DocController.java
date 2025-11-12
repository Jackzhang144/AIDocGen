package com.codecraft.aidoc.controller;

import com.codecraft.aidoc.common.ApiResponse;
import com.codecraft.aidoc.enums.ErrorCode;
import com.codecraft.aidoc.pojo.dto.JobStatusView;
import com.codecraft.aidoc.pojo.request.DocGenerationJobRequest;
import com.codecraft.aidoc.pojo.request.FeedbackRequest;
import com.codecraft.aidoc.pojo.request.IntroDiscoverRequest;
import com.codecraft.aidoc.pojo.request.IntroRequest;
import com.codecraft.aidoc.security.UserPrincipal;
import com.codecraft.aidoc.service.DocJobService;
import com.codecraft.aidoc.service.RateLimiterService;
import com.codecraft.aidoc.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Core documentation generation controller that exposes asynchronous job endpoints.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/docs")
public class DocController {

    private final DocJobService docJobService;
    private final RateLimiterService rateLimiterService;

    private static final long USER_RATE_WINDOW_SECONDS = 15 * 60;

    @PostMapping("/write/v3")
    public ApiResponse<Map<String, String>> writeSelection(@AuthenticationPrincipal UserPrincipal principal,
                                                           @Valid @RequestBody DocGenerationJobRequest request) {
        enrichRequestFromPrincipal(principal, request);
        enforceUserQuota(principal);
        String jobId = docJobService.submitSelectionJob(principal.getId(), request);
        return ApiResponse.ok("任务创建成功", Map.of("id", jobId));
    }

    @PostMapping("/write/v3/no-selection")
    public ApiResponse<Map<String, String>> writeWithoutSelection(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody DocGenerationJobRequest request) {
        enrichRequestFromPrincipal(principal, request);
        enforceUserQuota(principal);
        String jobId = docJobService.submitContextualJob(principal.getId(), request);
        return ApiResponse.ok("任务创建成功", Map.of("id", jobId));
    }

    @GetMapping("/worker/{id}")
    public ApiResponse<Map<String, Object>> getWorkerStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable("id") String jobId) {
        JobStatusView status = docJobService.getJob(principal.getId(), jobId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", status.getJobId());
        payload.put("state", status.getState());
        payload.put("reason", status.getReason());
        payload.put("data", status.getResult());
        return ApiResponse.ok("获取任务状态成功", payload);
    }

    @PostMapping("/feedback")
    public ApiResponse<Void> submitFeedback(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody FeedbackRequest request) {
        docJobService.recordFeedback(request.getId(), request.getFeedback());
        return ApiResponse.ok("反馈提交成功", null);
    }

    @PostMapping("/intro")
    public ApiResponse<Void> submitIntro(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody IntroRequest request) {
        docJobService.recordIntro(request.getId(), request.getPurpose());
        return ApiResponse.ok("问卷提交成功", null);
    }

    @PostMapping("/intro/discover")
    public ApiResponse<Void> submitIntroDiscover(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody IntroDiscoverRequest request) {
        docJobService.recordIntroDiscover(request.getId(), request.getSource());
        return ApiResponse.ok("渠道记录成功", null);
    }

    private void enrichRequestFromPrincipal(UserPrincipal principal, DocGenerationJobRequest request) {
        request.setUserId(principal.getUsername());
        request.setEmail(principal.getEmail());
        if (request.getSource() == null) {
            request.setSource("web");
        }
    }

    private void enforceUserQuota(UserPrincipal principal) {
        if (principal.getRole().isPremium()) {
            return;
        }
        int quota = principal.getApiQuota() != null ? principal.getApiQuota() : 50;
        if (quota < 0) {
            return;
        }
        boolean allowed = rateLimiterService.tryConsume("user-docs:" + principal.getId(), quota, USER_RATE_WINDOW_SECONDS);
        if (!allowed) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "已达到用户配额限制，请稍后再试");
        }
    }
}
