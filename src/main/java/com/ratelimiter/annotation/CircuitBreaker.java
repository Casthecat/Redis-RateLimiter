package com.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Circuit breaker annotation—apply to methods that require circuit breaker protection.
 * Opens circuit for openSeconds when error rate exceeds failureThreshold within windowSeconds.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CircuitBreaker {

    /**
     * Failure rate threshold (0.0 ~ 1.0); circuit opens when exceeded
     */
    double failureThreshold() default 0.5;

    /**
     * Statistics window in seconds
     */
    int windowSeconds() default 60;

    /**
     * Circuit open duration in seconds
     */
    int openSeconds() default 30;

    /**
     * Minimum number of requests before circuit can open
     */
    int minRequests() default 5;
}
