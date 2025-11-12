package com.codecraft.aidoc.pojo.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Projection of API keys for admin management UI.
 */
@Data
@Builder
public class ApiKeyResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String purpose;
    private LocalDateTime createdAt;
    private String hashedKey;
    private String rawKey;
}
