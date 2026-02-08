package com.ratelimiter;

import com.ratelimiter.exception.CircuitBreakerException;
import com.ratelimiter.exception.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Rate limiting and circuit breaker integration tests.
 * CircuitBreakerTests must run first (shared state); @Order controls Nested execution order.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class RateLimiterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // Default: allow requests
        when(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(),
                any()
        )).thenReturn(1L);
    }

    @Nested
    @Order(2)
    @DisplayName("@RateLimit tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RateLimitTests {

        @Test
        @DisplayName("Normal pass: Lua returns 1, expect Success!")
        void normalPass_shouldReturnSuccess() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(1L);

            mockMvc.perform(get("/api/test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success!"));
        }

        @Test
        @DisplayName("Rate limited with fallback: Lua returns 0, expect fallback message")
        void rateLimitedWithFallback_shouldReturnFallbackMessage() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Please wait, your request is queued..."));
        }

        @Test
        @DisplayName("Rate limited with non-existent fallback: expect 429")
        void rateLimitedWithBadFallback_shouldReturn429() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/test-bad-fallback"))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
        }

        @Test
        @DisplayName("test-no-fallback normal pass")
        void testNoFallback_normalPass() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(1L);

            mockMvc.perform(get("/api/test-no-fallback"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success!"));
        }

        @Test
        @DisplayName("Rate limited without fallback: Lua returns 0, expect 429 and JSON error")
        void rateLimitedWithoutFallback_shouldReturn429() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/test-no-fallback"))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.status").value(429))
                    .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"))
                    .andExpect(jsonPath("$.message").value(containsString("Too many")));
        }
    }

    @Nested
    @Order(1)
    @DisplayName("@CircuitBreaker tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CircuitBreakerTests {

        @Test
        @Order(2)
        @DisplayName("Closed → Open: after multiple failures, circuit opens and returns 503")
        void closedToOpen_shouldReturn503() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/test-circuit")).andExpect(status().isTooManyRequests());
            mockMvc.perform(get("/api/test-circuit")).andExpect(status().isTooManyRequests());

            mockMvc.perform(get("/api/test-circuit"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503))
                    .andExpect(jsonPath("$.code").value("CIRCUIT_BREAKER_OPEN"))
                    .andExpect(jsonPath("$.message").value(containsString("unavailable")));
        }

        @Test
        @Order(1)
        @DisplayName("test-circuit normal pass")
        void testCircuit_normalPass() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(1L);

            mockMvc.perform(get("/api/test-circuit"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success!"));
        }

        @Test
        @Order(3)
        @DisplayName("Open → Half-Open: after 1s, 3 successes restore Closed")
        void openToHalfOpen_shouldRecover() throws Exception {
            // Circuit already opened by closedToOpen; verify recovery: wait 1s then 3 successes to restore Closed
            Thread.sleep(1100);

            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(1L);

            mockMvc.perform(get("/api/test-circuit")).andExpect(status().isOk());
            mockMvc.perform(get("/api/test-circuit")).andExpect(status().isOk());
            mockMvc.perform(get("/api/test-circuit")).andExpect(status().isOk());
        }
    }

    @Nested
    @Order(3)
    @DisplayName("GlobalExceptionHandler tests")
    class ExceptionHandlerTests {

        @Test
        @DisplayName("RateLimitException returns 429 with correct JSON")
        void rateLimitException_shouldReturn429() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/test-no-fallback"))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(429))
                    .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("CircuitBreakerException returns 503 with correct JSON")
        void circuitBreakerException_shouldReturn503() throws Exception {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/test-circuit"));
            mockMvc.perform(get("/api/test-circuit"));

            mockMvc.perform(get("/api/test-circuit"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(503))
                    .andExpect(jsonPath("$.code").value("CIRCUIT_BREAKER_OPEN"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
