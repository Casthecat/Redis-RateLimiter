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
 * 限流 AOP 切面，拦截带 @RateLimit 注解的方法。
 * 注入预加载的 limitScript Bean，Redis 客户端自动使用 EVALSHA 执行，提升性能。
 */
@Aspect
@Component
@Order(2)  // 在 CircuitBreakerAspect 之后执行
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

        // 构建唯一 key：前缀 + 类名.方法名
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
            throw new RateLimitException("请求过于频繁，请稍后再试");
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
     * 反射调用降级方法，同类中的无参 public 方法。
     */
    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName) {
        try {
            Object target = joinPoint.getTarget();
            Method fallbackMethod = target.getClass().getMethod(fallbackMethodName);
            return fallbackMethod.invoke(target);
        } catch (Exception e) {
            throw new RateLimitException("请求过于频繁，请稍后再试", e);
        }
    }
}
