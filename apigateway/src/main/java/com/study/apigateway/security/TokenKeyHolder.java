package com.study.apigateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class TokenKeyHolder {

	private static final String BINDING_KEY = "token.public-keys";

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
		if (event.getKeys().stream().noneMatch(key -> key.startsWith("token."))) {
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

	@SuppressWarnings("unchecked")
	private TokenKeys bind() {
		Map<String, String> encoded = (Map<String, String>) Binder.get(environment)
				.bind(BINDING_KEY, Bindable.mapOf(String.class, String.class))
				.orElseThrow(() -> new IllegalArgumentException("token.public-keys configuration is missing"));

		return TokenKeys.from(encoded);
	}
}
