package com.codecraft.aidoc.pojo.response;

import com.codecraft.aidoc.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * View exposed on the admin panel for quick inspection of registered users.
 */
@Data
@Builder
public class UserSummaryResponse {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private Integer apiQuota;
    private LocalDateTime createdAt;
}
