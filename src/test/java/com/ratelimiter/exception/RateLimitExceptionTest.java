package com.ratelimiter.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateLimitException unit tests.
 */
class RateLimitExceptionTest {

    @Test
    @DisplayName("Constructor (message)")
    void constructorWithMessage() {
        RateLimitException ex = new RateLimitException("test message");
        assertThat(ex.getMessage()).isEqualTo("test message");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor (message, cause)")
    void constructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("cause");
        RateLimitException ex = new RateLimitException("test", cause);
        assertThat(ex.getMessage()).isEqualTo("test");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
