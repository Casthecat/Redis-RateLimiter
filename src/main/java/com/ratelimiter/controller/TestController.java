package com.ratelimiter.controller;

import com.ratelimiter.annotation.CircuitBreaker;
import com.ratelimiter.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rate limiting and circuit breaker test endpoints.
 */
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    @RateLimit(count = 3, time = 60, fallbackMethod = "myFallback")  // Max 3 requests per 60 seconds
    @CircuitBreaker(failureThreshold = 0.5, windowSeconds = 60, openSeconds = 30)  // Circuit opens if >50% failure rate in 1 min
    public String test() {
        return "Success!";
    }

    /**
     * Fallback method invoked when rate limit is triggered.
     */
    public String myFallback() {
        return "Please wait, your request is queued...";
    }

    /**
     * Rate limit only, no fallback—for testing RateLimitException and 429 response.
     */
    @GetMapping("/test-no-fallback")
    @RateLimit(count = 3, time = 60)
    public String testNoFallback() {
        return "Success!";
    }

    /**
     * Rate limit + circuit breaker—for testing CircuitBreaker state machine.
     */
    @GetMapping("/test-circuit")
    @RateLimit(count = 3, time = 60)
    @CircuitBreaker(failureThreshold = 0.5, windowSeconds = 60, openSeconds = 1, minRequests = 2)
    public String testCircuit() {
        return "Success!";
    }

    /**
     * Rate limit + non-existent fallback—for testing invokeFallback exception branch.
     */
    @GetMapping("/test-bad-fallback")
    @RateLimit(count = 3, time = 60, fallbackMethod = "nonExistentMethod")
    public String testBadFallback() {
        return "Success!";
    }
}
