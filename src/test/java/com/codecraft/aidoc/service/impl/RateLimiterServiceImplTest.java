package com.codecraft.aidoc.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures the in-memory rate limiter enforces limits as expected.
 */
class RateLimiterServiceImplTest {

    private final RateLimiterServiceImpl rateLimiter = new RateLimiterServiceImpl((StringRedisTemplate) null);

    @Test
    void respectsTokenBucketLimits() {
        String key = "test-key";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryConsume(key, 5, 60));
        }
        assertFalse(rateLimiter.tryConsume(key, 5, 60));
    }
}
