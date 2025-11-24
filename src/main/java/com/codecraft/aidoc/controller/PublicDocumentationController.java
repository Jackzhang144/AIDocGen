package com.codecraft.aidoc.controller;

import com.codecraft.aidoc.common.ApiResponse;
import com.codecraft.aidoc.enums.DocFormat;
import com.codecraft.aidoc.enums.LanguageId;
import com.codecraft.aidoc.enums.ErrorCode;
import com.codecraft.aidoc.exception.BusinessException;
import com.codecraft.aidoc.pojo.dto.DocGenerationResult;
import com.codecraft.aidoc.pojo.request.DocumentGenerationRequest;
import com.codecraft.aidoc.pojo.response.DocumentGenerationResponse;
import com.codecraft.aidoc.pojo.response.SupportedFormatsResponse;
import com.codecraft.aidoc.pojo.response.SupportedLanguagesResponse;
import com.codecraft.aidoc.service.DocumentationService;
import com.codecraft.aidoc.service.RateLimiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Public API compatible with the legacy Mintlify endpoints.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class PublicDocumentationController {

    private static final int RATE_LIMIT = 100;
    private static final long RATE_WINDOW_SECONDS = 15 * 60;

    private final DocumentationService documentationService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/document")
    public ApiResponse<DocumentGenerationResponse> generateDocumentation(@Valid @RequestBody DocumentGenerationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication != null ? String.valueOf(authentication.getPrincipal()) : "anonymous";
        if (!rateLimiterService.tryConsume("document:" + principal, RATE_LIMIT, RATE_WINDOW_SECONDS)) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "已达到 15 分钟内 100 次调用的限制");
        }

        DocGenerationResult result = documentationService.generateDocumentation(request);
        DocumentGenerationResponse response = DocumentGenerationResponse.builder()
                .documentation(result.getDocumentation())
                .annotatedCode(result.getAnnotatedCode())
                .rawComment(result.getRawComment())
                .lineComments(result.getLineComments())
                .docFormat(result.getDocFormat())
                .commentFormat(result.getCommentFormat())
                .modelProvider(result.getModelProvider())
                .inferenceLatencyMs(result.getInferenceLatencyMs())
                .build();
        return ApiResponse.ok("文档生成成功", response);
    }

    @GetMapping("/list/languages")
    public ApiResponse<SupportedLanguagesResponse> listLanguages() {
        SupportedLanguagesResponse response = SupportedLanguagesResponse.builder()
                .languages(LanguageId.publicLanguageIds())
                .build();
        return ApiResponse.ok("获取支持的语言列表成功", response);
    }

    @GetMapping("/list/formats")
    public ApiResponse<SupportedFormatsResponse> listFormats() {
        List<SupportedFormatsResponse.FormatDescriptor> descriptors = Arrays.stream(DocFormat.values())
                .filter(format -> format != DocFormat.AUTO_DETECT)
                .map(format -> SupportedFormatsResponse.FormatDescriptor.builder()
                        .id(format.getId())
                        .defaultLanguages(format.getDefaultLanguages())
                        .build())
                .collect(Collectors.toList());
        SupportedFormatsResponse response = SupportedFormatsResponse.builder()
                .formats(descriptors)
                .build();
        return ApiResponse.ok("获取支持的文档格式成功", response);
    }
}
