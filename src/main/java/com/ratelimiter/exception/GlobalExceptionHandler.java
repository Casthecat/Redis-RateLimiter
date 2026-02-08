package com.ratelimiter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 全局异常处理，限流时返回 HTTP 429 及清晰的 JSON 错误信息。
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
                        "message", ex.getMessage() != null ? ex.getMessage() : "请求过于频繁，请稍后再试"
                ));
    }

    @ExceptionHandler(CircuitBreakerException.class)
    public ResponseEntity<Map<String, Object>> handleCircuitBreakerException(CircuitBreakerException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "code", "CIRCUIT_BREAKER_OPEN",
                        "message", ex.getMessage() != null ? ex.getMessage() : "服务熔断中，请稍后再试"
                ));
    }
}
