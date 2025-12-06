package com.codecraft.repository;

import com.codecraft.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    long countByUsernameAndCreatedAtBetween(String username, LocalDateTime start, LocalDateTime end);
}
