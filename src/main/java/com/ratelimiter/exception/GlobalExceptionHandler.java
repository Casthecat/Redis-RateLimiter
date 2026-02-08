package com.ratelimiter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler—returns HTTP 429/503 with structured JSON error when rate limited or circuit open.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitException(RateLimitException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                        "status", 429,
                        "code", "TOO_MANY_REQUESTS",
                        "message", ex.getMessage() != null ? ex.getMessage() : "Too many requests, please try again later"
                ));
    }

    @ExceptionHandler(CircuitBreakerException.class)
    public ResponseEntity<Map<String, Object>> handleCircuitBreakerException(CircuitBreakerException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "code", "CIRCUIT_BREAKER_OPEN",
                        "message", ex.getMessage() != null ? ex.getMessage() : "Service temporarily unavailable, please try again later"
                ));
    }
}
