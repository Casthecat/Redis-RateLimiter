package com.ratelimiter.service;

/**
 * Rate limiting core service interface.
 */
public interface RateLimitService {

    /**
     * Check if request is allowed.
     *
     * @param key    Rate limit key
     * @param limit  Max requests allowed in the window
     * @param period Time window in seconds
     * @return true if allowed
     */
    boolean tryAcquire(String key, int limit, int period);
}
