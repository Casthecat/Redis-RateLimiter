package com.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * Redis configuration.
 * Preloads limit.lua as DefaultRedisScript Bean; Spring computes SHA1 at startup for caching.
 * Redis client uses EVALSHA for better performance.
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
