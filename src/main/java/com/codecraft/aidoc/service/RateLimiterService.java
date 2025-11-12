package com.codecraft.aidoc.service;

/**
 * Simple token bucket style rate limiter to guard public APIs from abuse.
 */
public interface RateLimiterService {

    /**
     * Attempts to consume a token for the supplied identifier.
     *
     * @param key identifier (api key hash, ip address, etc.)
     * @param limit maximum number of requests within the window
     * @param windowSeconds sliding window length in seconds
     * @return {@code true} when the request is allowed
     */
    boolean tryConsume(String key, int limit, long windowSeconds);
}
