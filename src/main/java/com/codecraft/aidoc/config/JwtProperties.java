package com.codecraft.aidoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration holder for JWT authentication settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Symmetric secret used to sign JWT tokens.
     */
    private String secret;

    /**
     * Token validity in milliseconds.
     */
    private long expirationMs;
}
