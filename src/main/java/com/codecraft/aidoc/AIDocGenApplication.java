package com.codecraft.aidoc;

import com.codecraft.aidoc.config.ModelGatewayProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Boot entrypoint that wires core infrastructure such as mapper scanning and caching support.
 */
@EnableCaching
@SpringBootApplication
@MapperScan("com.codecraft.aidoc.mapper")
@EnableConfigurationProperties(ModelGatewayProperties.class)
public class AIDocGenApplication {

    /**
     * Launches the rewritten backend service.
     *
     * @param args JVM startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AIDocGenApplication.class, args);
    }
}
