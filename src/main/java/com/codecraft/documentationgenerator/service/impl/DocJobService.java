package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.RequiresAuthenticationException;
import com.codecraft.documentationgenerator.model.GenerateDocRequest;
import com.codecraft.documentationgenerator.model.Synopsis;
import com.codecraft.documentationgenerator.service.AiDocumentationServiceInterface;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import com.codecraft.documentationgenerator.service.DocServiceInterface;
import com.codecraft.documentationgenerator.service.TeamServiceInterface;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import com.codecraft.documentationgenerator.service.jobs.DocJob;
import com.codecraft.documentationgenerator.service.jobs.DocJobResult;
import com.codecraft.documentationgenerator.service.jobs.JobState;
import com.codecraft.documentationgenerator.service.jobs.ShowFeedbackStatus;
import com.codecraft.documentationgenerator.util.CommentFormatter;
import com.codecraft.documentationgenerator.util.LanguageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 文档生成任务服务，负责调度异步生成并缓存任务状态
 */
@Slf4j
@Service
public class DocJobService {

    private static final int MAX_DOCS_FOR_AUTH = 60;
    private static final int DAYS_PER_QUOTA_PERIOD = 30;

    private final ExecutorService executorService;
    private final Map<String, DocJob> jobs = new ConcurrentHashMap<>();

    private final AiDocumentationServiceInterface aiDocumentationService;
    private final CodeParsingServiceInterface codeParsingService;
    private final DocServiceInterface docService;
    private final UserServiceInterface userService;
    private final TeamServiceInterface teamService;

    public DocJobService(AiDocumentationServiceInterface aiDocumentationService,
                         CodeParsingServiceInterface codeParsingService,
                         DocServiceInterface docService,
                         UserServiceInterface userService,
                         TeamServiceInterface teamService) {
        this(aiDocumentationService, codeParsingService, docService, userService, teamService,
                Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors())));
    }

    DocJobService(AiDocumentationServiceInterface aiDocumentationService,
                  CodeParsingServiceInterface codeParsingService,
                  DocServiceInterface docService,
                  UserServiceInterface userService,
                  TeamServiceInterface teamService,
                  ExecutorService executorService) {
        this.aiDocumentationService = aiDocumentationService;
        this.codeParsingService = codeParsingService;
        this.docService = docService;
        this.userService = userService;
        this.teamService = teamService;
        this.executorService = executorService;
    }

    /**
     * 提交一个文档生成任务
     *
     * @param request 生成请求
     * @return 任务ID
     */
    public String submitJob(GenerateDocRequest request) {
        validateAuthenticationRequirement(request);

        String jobId = UUID.randomUUID().toString();
        DocJob job = new DocJob(jobId);
        jobs.put(jobId, job);

        executorService.submit(() -> processJob(job, request));
        return jobId;
    }

    /**
     * 获取任务
     *
     * @param jobId 任务ID
     * @return 任务信息
     */
    public Optional<DocJob> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    private void processJob(DocJob job, GenerateDocRequest request) {
        job.markRunning();
        try {
            DocJobResult result = generateDocumentation(request);
            job.markCompleted(result);
        } catch (RequiresAuthenticationException authException) {
            job.markFailed(authException.getMessage());
            throw authException;
        } catch (Exception ex) {
            log.error("Failed to generate documentation", ex);
            job.markFailed(Optional.ofNullable(ex.getMessage()).orElse("Internal error"));
        }
    }

    private DocJobResult generateDocumentation(GenerateDocRequest request) {
        String originalLanguageId = Optional.ofNullable(request.getLanguageId()).orElse("unknown");
        String resolvedLanguageId = LanguageHelper.resolveLanguageId(request.getFileName(), originalLanguageId);

        String effectiveContext = Optional.ofNullable(request.getContext()).orElse(request.getCode());
        Synopsis synopsis = codeParsingService.getSynopsis(request.getCode(), resolvedLanguageId, effectiveContext);

        long startTime = System.nanoTime();
        long aiStart = System.nanoTime();
        String rawDocstring = generateDocstringBySynopsis(request.getCode(), synopsis, resolvedLanguageId);
        long aiEnd = System.nanoTime();

        String wrappedDocstring = wrapDocstring(rawDocstring, request);
        String formattedDocstring = applyCommentIfNeeded(wrappedDocstring, request, resolvedLanguageId, synopsis);

        DocJobResult result = new DocJobResult();
        result.setDocstring(formattedDocstring);
        result.setPreview(rawDocstring);
        result.setPosition(determineCommentPosition(resolvedLanguageId, synopsis.getKind()));

        String feedbackId = UUID.randomUUID().toString();
        result.setFeedbackId(feedbackId);

        long endTime = System.nanoTime();
        persistDoc(request, resolvedLanguageId, synopsis, formattedDocstring, feedbackId,
                Duration.ofNanos(aiEnd - aiStart), Duration.ofNanos(endTime - startTime));

        ShowFeedbackStatus feedbackStatus = evaluateFeedbackVisibility(request.getUserId(), result);
        result.setShouldShowFeedback(feedbackStatus.isShouldShowFeedback());
        result.setShouldShowShare(feedbackStatus.isShouldShowShare());

        touchUserLastActive(request.getUserId());

        return result;
    }

    private void validateAuthenticationRequirement(GenerateDocRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new RequiresAuthenticationException(
                    "Please update the extension to continue",
                    "🔐 Sign in",
                    "No userId provided"
            );
        }

        String source = Optional.ofNullable(request.getSource()).orElse("");
        if ("intellij".equalsIgnoreCase(source) || "web".equalsIgnoreCase(source)) {
            return;
        }

        LocalDateTime since = LocalDateTime.now().minusDays(DAYS_PER_QUOTA_PERIOD);
        int docsCount = docService.countDocsByUserSince(request.getUserId(), since);
        User identifiedUser = userService.findByEmailOrNull(request.getEmail());
        boolean belongsToTeam = request.getEmail() != null && teamService.findByEmail(request.getEmail()) != null;

        if (identifiedUser == null && !belongsToTeam && docsCount > MAX_DOCS_FOR_AUTH) {
            throw new RequiresAuthenticationException(
                    "Please sign in to continue. By doing so, you agree to Mintlify's terms and conditions",
                    "🔐 Sign in",
                    "Please update the extension to continue"
            );
        }
    }

    private String generateDocstringBySynopsis(String code, Synopsis synopsis, String languageId) {
        try {
            switch (Optional.ofNullable(synopsis.getKind()).orElse("unspecified")) {
                case "function":
                    return aiDocumentationService.generateFunctionDocstring(code, synopsis, languageId);
                case "class":
                    return aiDocumentationService.generateClassDocstring(code, languageId);
                default:
                    return aiDocumentationService.generateSimpleExplanation(code, languageId);
            }
        } catch (Exception ex) {
            log.warn("AI generation failed, using heuristic summary", ex);
            return fallbackDocstring(code, synopsis, languageId);
        }
    }

    private String wrapDocstring(String docstring, GenerateDocRequest request) {
        Integer width = request.getWidth();
        if (width == null || width <= 0) {
            return docstring;
        }
        return CommentFormatter.wrap(docstring, width);
    }

    private String applyCommentIfNeeded(String docstring, GenerateDocRequest request, String languageId, Synopsis synopsis) {
        if (!Boolean.TRUE.equals(request.getCommented())) {
            return docstring;
        }
        return CommentFormatter.addComments(docstring, languageId, synopsis.getKind());
    }

    private void persistDoc(GenerateDocRequest request,
                            String languageId,
                            Synopsis synopsis,
                            String output,
                            String feedbackId,
                            Duration aiDuration,
                            Duration totalDuration) {
        Doc doc = new Doc();
        doc.setUserId(request.getUserId());
        doc.setEmail(request.getEmail());
        doc.setOutput(output);
        doc.setPrompt("write:v3");
        doc.setLanguage(languageId);
        doc.setTimeToCall((int) aiDuration.toMillis());
        doc.setTimeToGenerate((int) totalDuration.toMillis());
        doc.setSource(request.getSource());
        doc.setFeedbackId(feedbackId);
        doc.setIsPreview(request.getAllowedKinds() != null && !request.getAllowedKinds().isEmpty());
        doc.setHasAcceptedPreview(false);
        doc.setIsExplained(true);
        doc.setDocFormat(Optional.ofNullable(request.getDocStyle()).orElse(request.getDocFormat()));
        doc.setCommentFormat(CommentFormatter.inferCommentFormat(languageId, synopsis.getKind(), doc.getDocFormat()));
        doc.setKind(Optional.ofNullable(synopsis.getKind()).orElse("unspecified"));
        doc.setIsSelection(Boolean.TRUE.equals(request.getIsSelection()));
        doc.setPromptId(UUID.randomUUID().toString());
        doc.setActualLanguage(languageId);
        doc.setTimestamp(LocalDateTime.now());
        docService.createDoc(doc);
    }

    private ShowFeedbackStatus evaluateFeedbackVisibility(String userId, DocJobResult result) {
        if (userId == null || userId.isEmpty()) {
            return new ShowFeedbackStatus(false, false);
        }

        try {
            if (docService.hasPositiveFeedback(userId)) {
                return new ShowFeedbackStatus(false, false);
            }

            var recentDocs = docService.findRecentDocs(userId, 3);
            if (recentDocs.isEmpty()) {
                return new ShowFeedbackStatus(false, false);
            }

            boolean hasRecentFeedback = recentDocs.stream().anyMatch(doc -> doc.getFeedback() != null);
            if (hasRecentFeedback) {
                return new ShowFeedbackStatus(false, false);
            }

            boolean withinProbability = ThreadLocalRandom.current().nextDouble() < 0.3;
            return new ShowFeedbackStatus(withinProbability, true);
        } catch (Exception ex) {
            log.warn("Failed to evaluate feedback visibility", ex);
            return new ShowFeedbackStatus(false, false);
        }
    }

    private void touchUserLastActive(String userUid) {
        if (userUid == null || userUid.isEmpty()) {
            return;
        }
        try {
            User user = userService.findByUserUid(userUid);
            userService.updateLastActive(user);
        } catch (Exception ignore) {
            // ignore if user not found
        }
    }

    private String determineCommentPosition(String languageId, String kind) {
        String normalizedKind = Optional.ofNullable(kind).orElse("unspecified");
        if ("python".equalsIgnoreCase(languageId) && "function".equalsIgnoreCase(normalizedKind)) {
            return "BelowStartLine";
        }
        return "Above";
    }

    private String fallbackDocstring(String code, Synopsis synopsis, String languageId) {
        String kind = Optional.ofNullable(synopsis.getKind()).orElse("code snippet");
        int length = Optional.ofNullable(code).map(String::length).orElse(0);
        return String.format("Documentation placeholder for %s (%s, %d chars)", kind, languageId, length);
    }
}
