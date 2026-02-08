package com.ratelimiter.exception;

/**
 * 熔断异常，当接口处于熔断状态时抛出。
 */
public class CircuitBreakerException extends RuntimeException {

    public CircuitBreakerException(String message) {
        super(message);
    }

    public CircuitBreakerException(String message, Throwable cause) {
        super(message, cause);
    }
}
