package com.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rate limiting annotation—apply to methods that require rate limiting.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Rate limit key prefix, used to distinguish different endpoints
     */
    String key() default "rate:limit:";

    /**
     * Maximum number of requests allowed within the time window
     */
    int count() default 10;

    /**
     * Time window in seconds
     */
    int time() default 60;

    /**
     * Fallback method name when rate limit is triggered—must be a public method in the same class. Empty means throw exception.
     */
    String fallbackMethod() default "";
}
