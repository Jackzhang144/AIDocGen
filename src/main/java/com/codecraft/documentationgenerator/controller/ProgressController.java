package com.codecraft.documentationgenerator.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码进度分析接口
 */
@Slf4j
@RestController
@RequestMapping("/progress")
public class ProgressController {

    @PostMapping
    public ResponseEntity<ProgressResponse> getProgress(@RequestBody ProgressRequest request) {
        log.info("Progress analysis requested for file {} (language={}, indicators={})",
                request.getFileName(), request.getLanguageId(), request.getTypes());
        ProgressResponse response = calculateProgress(request.getFile(), request.getTypes());
        log.debug("Progress response total={} for file {}", response.getTotal(), request.getFileName());
        return ResponseEntity.ok(response);
    }

    private ProgressResponse calculateProgress(String code, List<ProgressIndicator> indicators) {
        if (code == null) {
            code = "";
        }

        List<ProgressIndicator> requestedIndicators =
                indicators == null || indicators.isEmpty() ? List.of(ProgressIndicator.values()) : indicators;

        Map<ProgressIndicator, ProgressResponse.Breakdown> breakdown = new EnumMap<>(ProgressIndicator.class);

        int total = 0;
        for (ProgressIndicator indicator : requestedIndicators) {
            int count = countOccurrences(code, indicator);
            ProgressResponse.Breakdown stats = new ProgressResponse.Breakdown();
            stats.setCurrent(0);
            stats.setTotal(count);
            breakdown.put(indicator, stats);
            total += count;
        }

        ProgressResponse response = new ProgressResponse();
        response.setCurrent(0);
        response.setTotal(total);
        response.setBreakdown(breakdown);
        return response;
    }

    private int countOccurrences(String code, ProgressIndicator indicator) {
        Pattern pattern;
        switch (indicator) {
            case Methods:
            case Functions:
                pattern = Pattern.compile("\\b(def|function|fun|async\\s+function|[a-zA-Z0-9_]+\\s*\\([^)]*\\)\\s*\\{)");
                break;
            case Classes:
                pattern = Pattern.compile("\\bclass\\s+[A-Za-z0-9_]+");
                break;
            case Types:
                pattern = Pattern.compile("\\b(type|interface|enum)\\s+[A-Za-z0-9_]+");
                break;
            default:
                pattern = Pattern.compile("$");
        }

        Matcher matcher = pattern.matcher(code);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public enum ProgressIndicator {
        Functions,
        Methods,
        Classes,
        Types
    }

    @Data
    public static class ProgressRequest {
        private String file;
        private String languageId;
        private List<ProgressIndicator> types;
        private String fileName;
    }

    @Data
    public static class ProgressResponse {
        private int current;
        private int total;
        private Map<ProgressIndicator, Breakdown> breakdown;

        @Data
        public static class Breakdown {
            private int current;
            private int total;
        }
    }
}
