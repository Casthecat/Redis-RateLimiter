package com.ratelimiter.exception;

/**
 * 限流异常，当请求超过限流阈值时抛出。
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
