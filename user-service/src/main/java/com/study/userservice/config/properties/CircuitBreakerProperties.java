package com.study.userservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "resilience.circuit-breaker")
public record CircuitBreakerProperties(
        float failureRateThreshold,
        Duration waitDurationInOpenState,
        int slidingWindowSize,
        Duration timeout) {
}
