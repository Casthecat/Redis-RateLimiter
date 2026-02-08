package com.ratelimiter.aspect;

import com.ratelimiter.annotation.RateLimit;
import com.ratelimiter.exception.RateLimitException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Rate limiting AOP aspect—intercepts methods annotated with @RateLimit.
 * Injects preloaded limitScript Bean; Redis client uses EVALSHA for better performance.
 */
@Aspect
@Component
@Order(2)  // Execute after CircuitBreakerAspect
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> limitScript;

    @Autowired
    public RateLimitAspect(StringRedisTemplate redisTemplate, DefaultRedisScript<Long> limitScript) {
        this.redisTemplate = redisTemplate;
        this.limitScript = limitScript;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key();
        int count = rateLimit.count();
        int time = rateLimit.time();

        // Build unique key: prefix + className.methodName
        String fullKey = key + getMethodKey(joinPoint);

        List<String> keys = Collections.singletonList(fullKey);

        Long result = redisTemplate.execute(
                limitScript,
                keys,
                String.valueOf(count),
                String.valueOf(time)
        );

        if (result != null && result == 0) {
            String fallbackMethod = rateLimit.fallbackMethod();
            if (fallbackMethod != null && !fallbackMethod.isBlank()) {
                return invokeFallback(joinPoint, fallbackMethod);
            }
            throw new RateLimitException("Too many requests, please try again later");
        }

        return joinPoint.proceed();
    }

    private String getMethodKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        return className + "." + methodName;
    }

    /**
     * Invoke fallback method via reflection—target class must have a no-arg public method.
     */
    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName) {
        try {
            Object target = joinPoint.getTarget();
            Method fallbackMethod = target.getClass().getMethod(fallbackMethodName);
            return fallbackMethod.invoke(target);
        } catch (Exception e) {
            throw new RateLimitException("Too many requests, please try again later", e);
        }
    }
}
