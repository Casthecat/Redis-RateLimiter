# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-02-08

### Added

- Core distributed rate limiting logic using Redis + Lua.
- Sliding window circuit breaker with automated state recovery.
- Support for `fallbackMethod` to provide graceful service degradation.
- Comprehensive test suite with 96% JaCoCo code coverage.
- Pre-loaded Lua script via `DefaultRedisScript` Bean for EVALSHA optimization.
- Global exception handler returning structured JSON (429/503).
