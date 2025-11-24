package com.codecraft.aidoc.pojo.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Projection of provider configurations for admin management UI.
 */
@Data
@Builder
public class ApiKeyResponse {

    private Long id;
    private String provider;
    private String baseUrl;
    private String model;
    private Double temperature;
    private Integer maxOutputTokens;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
