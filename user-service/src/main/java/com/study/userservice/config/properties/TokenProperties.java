package com.study.userservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "token")
public record TokenProperties(String secret, Duration expirationTime) {

    private static final int MINIMUM_SECRET_BYTES = 64;

    public TokenProperties {
        if (secret == null || secret.getBytes().length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException("token.secret must be at least " + MINIMUM_SECRET_BYTES + " bytes for HS512 signing");
        }
        if (expirationTime == null || expirationTime.isNegative() || expirationTime.isZero()) {
            throw new IllegalArgumentException("token.expiration-time must be a positive duration");
        }
    }
}
