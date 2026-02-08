package com.ratelimiter.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CircuitBreakerException unit tests.
 */
class CircuitBreakerExceptionTest {

    @Test
    @DisplayName("Constructor (message)")
    void constructorWithMessage() {
        CircuitBreakerException ex = new CircuitBreakerException("Circuit open");
        assertThat(ex.getMessage()).isEqualTo("Circuit open");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor (message, cause)")
    void constructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("cause");
        CircuitBreakerException ex = new CircuitBreakerException("Circuit open", cause);
        assertThat(ex.getMessage()).isEqualTo("Circuit open");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
