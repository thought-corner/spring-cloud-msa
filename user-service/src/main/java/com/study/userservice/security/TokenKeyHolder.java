package com.study.userservice.security;

import com.study.userservice.config.properties.TokenKeyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class TokenKeyHolder {

	private static final String BINDING_PREFIX = "token";

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
			log.info("Token keys refreshed. active kid: {}, verification kids: {}",
					refreshed.activeKid(), refreshed.publicKeys().keySet());
		} catch (RuntimeException ex) {
			log.warn("Token key refresh rejected, keeping previous keys: {}", ex.getMessage());
		}
	}

	private TokenKeys bind() {
		TokenKeyProperties properties = Binder.get(environment)
				.bind(BINDING_PREFIX, TokenKeyProperties.class)
				.orElseThrow(() -> new IllegalArgumentException("token.* configuration is missing"));

		return TokenKeys.from(properties);
	}
}
