package com.codecraft.aidoc.service.impl;

import com.codecraft.aidoc.service.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 支持 Redis 与内存双实现的限流器：优先使用 Redis 分布式脚本，失败时自动降级到本地桶控制。
 */
@Slf4j
@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final class Bucket {
        private final AtomicInteger counter = new AtomicInteger();
        private volatile long windowStartedAt;
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> rateLimiterScript;

    @Autowired
    public RateLimiterServiceImpl(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this(redisTemplateProvider != null ? redisTemplateProvider.getIfAvailable() : null);
    }

    RateLimiterServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        if (redisTemplate != null) {
            this.rateLimiterScript = new DefaultRedisScript<>();
            this.rateLimiterScript.setResultType(Long.class);
            this.rateLimiterScript.setScriptText(buildScript());
        } else {
            this.rateLimiterScript = null;
        }
    }

    @Override
    public boolean tryConsume(String key, int limit, long windowSeconds) {
        if (redisTemplate != null && rateLimiterScript != null) {
            try {
                String namespacedKey = "aidoc:rate:" + key;
                long now = Instant.now().getEpochSecond();
                Long allowed = redisTemplate.execute(rateLimiterScript, List.of(namespacedKey),
                        String.valueOf(limit), String.valueOf(windowSeconds), String.valueOf(now));
                if (allowed != null) {
                    boolean success = allowed == 1L;
                    if (!success) {
                        log.warn("[Aidoc] Redis 限流触发 key={} limit={}", key, limit);
                    }
                    return success;
                }
            } catch (Exception ex) {
                log.warn("[Aidoc] Redis 限流执行异常，自动回退到内存模式", ex);
            }
        }
        return tryConsumeInMemory(key, limit, windowSeconds);
    }

    private boolean tryConsumeInMemory(String key, int limit, long windowSeconds) {
        final long now = Instant.now().getEpochSecond();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> {
            Bucket created = new Bucket();
            created.windowStartedAt = now;
            return created;
        });

        synchronized (bucket) {
            if (now - bucket.windowStartedAt >= windowSeconds) {
                bucket.windowStartedAt = now;
                bucket.counter.set(0);
            }
            int value = bucket.counter.incrementAndGet();
            if (value > limit) {
                log.warn("[Aidoc] 内存限流触发 key={} limit={}", key, limit);
                return false;
            }
            return true;
        }
    }

    private String buildScript() {
        return """
                local key = KEYS[1]
                local limit = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local now = tonumber(ARGV[3])
                redis.call('ZREMRANGEBYSCORE', key, '-inf', now - window)
                local current = redis.call('ZCARD', key)
                if current >= limit then
                  return 0
                end
                redis.call('ZADD', key, now, now .. '-' .. math.random())
                redis.call('EXPIRE', key, window)
                return 1
                """;
    }
}
