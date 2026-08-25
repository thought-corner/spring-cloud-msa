package com.study.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 공개키 스냅샷을 보관하고, Bus(busrefresh)로 전파된 설정 변경 시 새 스냅샷으로 교체한다.
 * 갱신 중 잘못된 키가 오면 기존 스냅샷을 유지한다(fail-safe). 발급자의 키 로테이션이
 * config→Bus 를 타고 검증자까지 무중단으로 전파된다.
 */
public class TokenKeyHolder {

    private static final Logger log = LoggerFactory.getLogger(TokenKeyHolder.class);

    private static final String BINDING_PREFIX = "token";
    private static final String BINDING_KEY = BINDING_PREFIX + ".public-keys";

    private final Environment environment;
    private final AtomicReference<TokenKeys> current = new AtomicReference<>();

    public TokenKeyHolder(Environment environment) {
        this.environment = environment;
        this.current.set(bind());
    }

    public TokenKeys current() {
        return current.get();
    }

    @EventListener
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        if (event.getKeys().stream().noneMatch(key -> key.startsWith(BINDING_PREFIX + "."))) {
            return;
        }
        try {
            TokenKeys refreshed = bind();
            current.set(refreshed);
            log.info("Token verification keys refreshed. kids: {}", refreshed.publicKeys().keySet());
        } catch (RuntimeException ex) {
            log.warn("Token key refresh rejected, keeping previous keys: {}", ex.getMessage());
        }
    }

    private TokenKeys bind() {
        Map<String, String> encoded = Binder.get(environment)
                .bind(BINDING_KEY, Bindable.mapOf(String.class, String.class))
                .orElseThrow(() -> new IllegalArgumentException("token.public-keys configuration is missing"));

        return TokenKeys.from(encoded);
    }
}
