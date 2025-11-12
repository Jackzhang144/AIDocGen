package com.codecraft.aidoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds default administrator credentials for bootstrapping.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.admin")
public class AdminProperties {

    private String username;
    private String password;
    private String email;
}
