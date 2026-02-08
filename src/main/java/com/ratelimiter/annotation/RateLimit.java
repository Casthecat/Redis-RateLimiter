package com.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解，标注在需要限流的方法上。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流 key 前缀，用于区分不同接口
     */
    String key() default "rate:limit:";

    /**
     * 时间窗口内允许的最大请求次数
     */
    int count() default 10;

    /**
     * 时间窗口（秒）
     */
    int time() default 60;

    /**
     * 限流触发时的降级方法名，同类中的 public 方法。为空则抛出异常。
     */
    String fallbackMethod() default "";
}
