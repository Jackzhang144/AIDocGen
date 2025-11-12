package com.codecraft.aidoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configures the thread pool used for asynchronous documentation generation jobs.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Creates a tuned executor that balances throughput with resource consumption for background tasks.
     *
     * @return thread pool executor bean
     */
    @Bean(name = "docGenerationExecutor")
    public ThreadPoolTaskExecutor docGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("doc-gen-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }
}
