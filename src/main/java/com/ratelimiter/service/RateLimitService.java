package com.ratelimiter.service;

/**
 * 限流核心逻辑服务接口。
 */
public interface RateLimitService {

    /**
     * 检查是否允许请求通过。
     *
     * @param key    限流 key
     * @param limit  时间窗口内允许的请求数
     * @param period 时间窗口（秒）
     * @return 是否允许通过
     */
    boolean tryAcquire(String key, int limit, int period);
}
