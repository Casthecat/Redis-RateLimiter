package com.ratelimiter;

import com.ratelimiter.exception.CircuitBreakerException;
import com.ratelimiter.exception.GlobalExceptionHandler;
import com.ratelimiter.exception.RateLimitException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler unit tests—covers all branches including null message.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("RateLimitException with message returns that message")
    void handleRateLimitException_withMessage() {
        ResponseEntity<Map<String, Object>> result = handler.handleRateLimitException(
                new RateLimitException("Custom rate limit message")
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(result.getBody()).containsEntry("status", 429);
        assertThat(result.getBody()).containsEntry("code", "TOO_MANY_REQUESTS");
        assertThat(result.getBody()).containsEntry("message", "Custom rate limit message");
    }

    @Test
    @DisplayName("RateLimitException with null message returns default")
    void handleRateLimitException_withNullMessage() {
        ResponseEntity<Map<String, Object>> result = handler.handleRateLimitException(
                new RateLimitException((String) null)
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(result.getBody()).containsEntry("status", 429);
        assertThat(result.getBody()).containsEntry("code", "TOO_MANY_REQUESTS");
        assertThat(result.getBody()).containsEntry("message", "Too many requests, please try again later");
    }

    @Test
    @DisplayName("CircuitBreakerException with message returns that message")
    void handleCircuitBreakerException_withMessage() {
        ResponseEntity<Map<String, Object>> result = handler.handleCircuitBreakerException(
                new CircuitBreakerException("Service unavailable")
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(result.getBody()).containsEntry("status", 503);
        assertThat(result.getBody()).containsEntry("code", "CIRCUIT_BREAKER_OPEN");
        assertThat(result.getBody()).containsEntry("message", "Service unavailable");
    }

    @Test
    @DisplayName("CircuitBreakerException with null message returns default")
    void handleCircuitBreakerException_withNullMessage() {
        ResponseEntity<Map<String, Object>> result = handler.handleCircuitBreakerException(
                new CircuitBreakerException((String) null)
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(result.getBody()).containsEntry("status", 503);
        assertThat(result.getBody()).containsEntry("code", "CIRCUIT_BREAKER_OPEN");
        assertThat(result.getBody()).containsEntry("message", "Service temporarily unavailable, please try again later");
    }
}
