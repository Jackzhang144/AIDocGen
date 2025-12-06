package com.codecraft.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_request_logs")
@Data
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestType; // comment, explain, document
    private String fileName;
    private String username;

    // TEXT 类型，防止代码片段过长
    @Column(columnDefinition = "TEXT")
    private String promptSnippet;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
