package com.ratelimiter.controller;

import com.ratelimiter.annotation.CircuitBreaker;
import com.ratelimiter.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 限流与熔断测试接口。
 */
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    @RateLimit(count = 3, time = 60, fallbackMethod = "myFallback")  // 60秒内最多3次
    @CircuitBreaker(failureThreshold = 0.5, windowSeconds = 60, openSeconds = 30)  // 1分钟内错误率>50%则熔断30秒
    public String test() {
        return "Success!";
    }

    /**
     * 限流触发时的降级方法。
     */
    public String myFallback() {
        return "排队中，请稍后...";
    }
}
