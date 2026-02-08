package com.ratelimiter.aspect;

import com.ratelimiter.annotation.CircuitBreaker;
import com.ratelimiter.exception.CircuitBreakerException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Circuit breaker AOP aspect—uses Closed / Open / Half-Open state machine.
 * Transitions to Open when error rate exceeds 50% within 1 minute; remains open for 30 seconds.
 */
@Aspect
@Component
@Order(1)  // Execute before RateLimitAspect; reject immediately when circuit is open
public class CircuitBreakerAspect {

    private final ConcurrentHashMap<String, CircuitState> stateMap = new ConcurrentHashMap<>();

    @Around("@annotation(circuitBreaker)")
    public Object around(ProceedingJoinPoint joinPoint, CircuitBreaker circuitBreaker) throws Throwable {
        String key = getMethodKey(joinPoint);
        double failureThreshold = circuitBreaker.failureThreshold();
        int windowSeconds = circuitBreaker.windowSeconds();
        int openSeconds = circuitBreaker.openSeconds();
        int minRequests = circuitBreaker.minRequests();

        CircuitState state = stateMap.computeIfAbsent(key, k -> new CircuitState());

        // Check if transitioning from Open to Half-Open (past open duration)
        state.tryTransitionFromOpen(openSeconds);

        // Open state: reject immediately
        if (state.isOpen()) {
            throw new CircuitBreakerException("Service temporarily unavailable, please try again later");
        }

        try {
            Object result = joinPoint.proceed();
            state.recordSuccess(failureThreshold, windowSeconds, minRequests);
            return result;
        } catch (Throwable t) {
            state.recordFailure(failureThreshold, windowSeconds, minRequests, openSeconds);
            throw t;
        }
    }

    private String getMethodKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getName();
    }

    /**
     * Circuit breaker states: Closed / Open / Half-Open
     */
    private static class CircuitState {
        private volatile State state = State.CLOSED;
        private int failureCount;
        private int totalCount;
        private long windowStartTime;
        private long openTime;
        private int halfOpenSuccessCount;

        private static final int HALF_OPEN_SUCCESS_THRESHOLD = 3;

        enum State {
            CLOSED,   // Normal
            OPEN,     // Tripped
            HALF_OPEN // Probing
        }

        synchronized boolean isOpen() {
            return state == State.OPEN;
        }

        synchronized void tryTransitionFromOpen(int openSeconds) {
            if (state == State.OPEN && System.currentTimeMillis() - openTime >= openSeconds * 1000L) {
                state = State.HALF_OPEN;
                halfOpenSuccessCount = 0;
            }
        }

        synchronized void recordSuccess(double failureThreshold, int windowSeconds, int minRequests) {
            if (state == State.HALF_OPEN) {
                halfOpenSuccessCount++;
                if (halfOpenSuccessCount >= HALF_OPEN_SUCCESS_THRESHOLD) {
                    state = State.CLOSED;
                    resetWindow();
                }
                return;
            }

            maybeResetWindow(windowSeconds);
            totalCount++;
        }

        synchronized void recordFailure(double failureThreshold, int windowSeconds, int minRequests, int openSeconds) {
            if (state == State.HALF_OPEN) {
                state = State.OPEN;
                openTime = System.currentTimeMillis();
                resetWindow();
                return;
            }

            maybeResetWindow(windowSeconds);
            totalCount++;
            failureCount++;

            if (totalCount >= minRequests && (double) failureCount / totalCount > failureThreshold) {
                state = State.OPEN;
                openTime = System.currentTimeMillis();
            }
        }

        private void maybeResetWindow(int windowSeconds) {
            long now = System.currentTimeMillis();
            if (now - windowStartTime >= windowSeconds * 1000L) {
                resetWindow();
            }
        }

        private void resetWindow() {
            failureCount = 0;
            totalCount = 0;
            windowStartTime = System.currentTimeMillis();
        }
    }
}
