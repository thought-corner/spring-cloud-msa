package com.study.userservice.config.properties;

import java.time.Duration;
import java.util.Map;

public record TokenKeyProperties(
        String activeKid,
        Map<String, String> publicKeys,
        Map<String, String> privateKeys,
        Duration expirationTime) {
}
