package com.codecraft.controller;

import com.codecraft.entity.User;
import com.codecraft.entity.UserRole;
import com.codecraft.repository.RequestLogRepository;
import com.codecraft.repository.UserRepository;
import com.codecraft.service.DeepSeekService;
import com.codecraft.service.SafetyService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
// 允许前端开发端口跨域
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AiController {

    private final DeepSeekService deepSeekService;
    private final UserRepository userRepository;
    private final RequestLogRepository requestLogRepository;
    private final SafetyService safetyService;
    private final long dailyLimit;

    public AiController(DeepSeekService deepSeekService,
                        UserRepository userRepository,
                        RequestLogRepository requestLogRepository,
                        SafetyService safetyService,
                        @Value("${ai.daily-limit:100}") long dailyLimit) {
        this.deepSeekService = deepSeekService;
        this.userRepository = userRepository;
        this.requestLogRepository = requestLogRepository;
        this.safetyService = safetyService;
        this.dailyLimit = dailyLimit;
    }

    @PostMapping("/process")
    public ResponseEntity<Mono<String>> processCode(@RequestBody AiRequest request,
                                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Mono.just("未登录"));
        }
        if (user.getRole() == UserRole.USER) {
            var start = java.time.LocalDate.now().atStartOfDay();
            var end = start.plusDays(1);
            long count = requestLogRepository.countByUsernameAndCreatedAtBetween(user.getUsername(), start, end);
            if (count >= dailyLimit) {
                return ResponseEntity.status(429).body(Mono.just("今日调用次数已达上限"));
            }
        }

        return ResponseEntity.ok(deepSeekService.callAi(
                request.getType(),
                request.getCode(),
                request.getFileName(),
                request.getContext(),
                request.getLanguage(),
                user.getUsername(),
                request.getFilePath(),
                request.getFramework(),
                request.getEditorLanguage()
        ));
    }

    @GetMapping("/usage")
    public ResponseEntity<UsageResponse> usage(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        var start = java.time.LocalDate.now().atStartOfDay();
        var end = start.plusDays(1);
        long today = requestLogRepository.countByUsernameAndCreatedAtBetween(user.getUsername(), start, end);
        var startEpoch = java.time.LocalDateTime.of(1970, 1, 1, 0, 0);
        var endEpoch = java.time.LocalDateTime.of(9999, 12, 31, 23, 59);
        long total = requestLogRepository.countByUsernameAndCreatedAtBetween(user.getUsername(), startEpoch, endEpoch);
        UsageResponse res = new UsageResponse();
        res.setUsername(user.getUsername());
        res.setRole(user.getRole().name());
        res.setDailyLimit(user.getRole() == UserRole.USER ? dailyLimit : -1);
        res.setTodayUsed(today);
        res.setTotalUsed(total);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/safety-check")
    public ResponseEntity<SafetyResponse> safety(@RequestBody SafetyRequest request,
                                                 @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        SafetyResponse res = new SafetyResponse();
        res.setWarnings(safetyService.checkDangerousPatterns(request.getContent()));
        return ResponseEntity.ok(res);
    }

    private User authUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7);
            String username = com.codecraft.security.JwtUtilStatic.parseUsername(token);
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Data
    static class AiRequest {
        private String type; // comment, explain, document
        private String code;
        private String fileName;
        private String context;
        private String language; // zh or en
        private String filePath;
        private String framework;
        private String editorLanguage;
    }

    @Data
    static class UsageResponse {
        private String username;
        private String role;
        private long todayUsed;
        private long totalUsed;
        private long dailyLimit; // -1 for unlimited
    }

    @Data
    static class SafetyRequest {
        private String content;
    }

    @Data
    static class SafetyResponse {
        private java.util.List<String> warnings;
    }
}
