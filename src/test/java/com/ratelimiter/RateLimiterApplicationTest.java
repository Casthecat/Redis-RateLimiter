package com.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies Spring context loads successfully.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
@ActiveProfiles("test")
class RateLimiterApplicationTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertThat(true).isTrue();
    }
}
