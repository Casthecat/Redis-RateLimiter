package com.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * Redis 配置类。
 * 将 limit.lua 预加载为 DefaultRedisScript Bean，Spring 启动时计算 SHA1 并缓存，
 * 执行时 Redis 客户端自动使用 EVALSHA 提升性能。
 */
@Configuration
public class RedisConfig {

    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("limit.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
