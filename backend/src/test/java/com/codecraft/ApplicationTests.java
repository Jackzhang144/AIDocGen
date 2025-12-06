package com.codecraft;

import com.codecraft.entity.RequestLog;
import com.codecraft.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Test
    void contextLoads() {
        // SpringBootTest will fail if critical beans are missing
    }

    @Test
    void savesRequestLogToTestDb() {
        RequestLog log = new RequestLog();
        log.setRequestType("explain");
        log.setFileName("Demo.java");
        log.setPromptSnippet("class Demo {}");

        RequestLog saved = requestLogRepository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
