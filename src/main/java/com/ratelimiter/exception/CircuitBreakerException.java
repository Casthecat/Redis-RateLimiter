package com.ratelimiter.exception;

/**
 * Thrown when endpoint is in circuit breaker open state.
 */
public class CircuitBreakerException extends RuntimeException {

    public CircuitBreakerException(String message) {
        super(message);
    }

    public CircuitBreakerException(String message, Throwable cause) {
        super(message, cause);
    }
}
