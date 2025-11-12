package com.codecraft.aidoc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties describing the CORS policy exposed by the HTTP layer.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "web.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));

    private List<String> allowedMethods = new ArrayList<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    private boolean allowCredentials = false;

    private long maxAge = 3600;
}
