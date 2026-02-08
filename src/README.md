# Distributed-Resilience-Limiter

## Project Overview

This project is a lightweight, high-performance distributed traffic governance component built on Spring Boot, Redis, and Lua. It provides robust protection against traffic surges and ensures system stability through automated circuit breaking and graceful fallback mechanisms.

## Core Features

- **Atomic Distributed Rate Limiting**: Implemented via Redis and Lua scripts to ensure atomicity in clustered environments, preventing race conditions and counter inaccuracies.

- **Performance Optimization**: Supports Lua script SHA pre-loading to minimize network overhead by transmitting 40-character SHA digests instead of full script strings.

- **Sliding Window Circuit Breaker**: Utilizes a three-state machine (Closed, Open, Half-Open) based on sliding window statistics for automated fault detection and recovery.

- **Non-Intrusive Design**: Deeply integrated with Spring AOP, allowing developers to enable protection by simply adding `@RateLimit` or `@CircuitBreaker` annotations to methods.

- **Graceful Fallback**: Supports reflection-based execution of custom fallback methods to return user-friendly responses during traffic interception.

## System Architecture

1. **AOP Layer**: Intercepts requests targeting annotated methods.
2. **Limiting Layer**: Calls Redis to execute Lua scripts for atomic counting.
3. **Breaker Layer**: Monitors error ratios and manages state transitions.
4. **Fallback Layer**: Executes designated `fallbackMethod` upon interception.

## Testing and Quality Assurance

This project maintains high standards for code quality. Comprehensive integration tests were implemented using JUnit 5 and Mockito.

- **JaCoCo Coverage Report**: Core package coverage: aspect (97%), exception (100%), config (100%).
![alt text](<../docs/images/test coverage.png>)
- **Verification Scenarios**: Tests cover distributed environment simulations, automated circuit breaker recovery, and reflection-based fallback execution.

## Quick Start

### 1. Configuration

Configure the Redis connection in your `application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 2. Basic Usage

```java
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/data")
    @RateLimit(count = 5, time = 60, fallbackMethod = "onRateLimit")
    @CircuitBreaker(failureThreshold = 0.5, windowSeconds = 60, openSeconds = 30)
    public String getData() {
        return "Business Data";
    }

    public String onRateLimit() {
        return "System busy, please try again later.";
    }
}
```

## License

This project is licensed under the MIT License. See [LICENSE](../LICENSE) for details.
