package com.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 熔断注解，标注在需要熔断保护的方法上。
 * 过去 windowSeconds 秒内报错比例超过 failureThreshold 则熔断 openSeconds 秒。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CircuitBreaker {

    /**
     * 失败比例阈值（0.0 ~ 1.0），超过则熔断
     */
    double failureThreshold() default 0.5;

    /**
     * 统计窗口（秒）
     */
    int windowSeconds() default 60;

    /**
     * 熔断时长（秒）
     */
    int openSeconds() default 30;

    /**
     * 最小请求数，低于此值不触发熔断
     */
    int minRequests() default 5;
}
