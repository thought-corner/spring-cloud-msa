package com.study.userservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.mock.env.MockEnvironment;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenKeyHolderTest {

	private static final KeyPair K1 = generateKeyPair();
	private static final KeyPair K2 = generateKeyPair();

	@Test
	void 잘못된_키로_갱신되면_기존_스냅샷을_유지한다() {
		MockEnvironment environment = validEnvironment();
		TokenKeyHolder holder = new TokenKeyHolder(environment);
		TokenKeys before = holder.current();

		environment.setProperty("token.public-keys.k1", "not-a-key");
		holder.onEnvironmentChange(new EnvironmentChangeEvent(Set.of("token.public-keys.k1")));

		assertThat(holder.current()).isSameAs(before);
	}

	@Test
	void 새_kid로_전환하면_스냅샷이_교체되고_구_키_검증도_유지된다() {
		MockEnvironment environment = validEnvironment();
		TokenKeyHolder holder = new TokenKeyHolder(environment);

		environment.setProperty("token.active-kid", "k2");
		environment.setProperty("token.public-keys.k2", encode(K2.getPublic().getEncoded()));
		environment.setProperty("token.private-keys.k2", encode(K2.getPrivate().getEncoded()));
		holder.onEnvironmentChange(new EnvironmentChangeEvent(Set.of("token.active-kid")));

		assertThat(holder.current().activeKid()).isEqualTo("k2");
		assertThat(holder.current().publicKeys().keySet()).containsExactlyInAnyOrder("k1", "k2");
	}

	@Test
	void 활성_kid의_개인키가_없으면_기동_시_거부된다() {
		MockEnvironment environment = validEnvironment();
		environment.setProperty("token.active-kid", "k9");

		assertThatThrownBy(() -> new TokenKeyHolder(environment))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("private-keys");
	}

	@Test
	void 관련_없는_설정_변경은_무시한다() {
		MockEnvironment environment = validEnvironment();
		TokenKeyHolder holder = new TokenKeyHolder(environment);
		TokenKeys before = holder.current();

		holder.onEnvironmentChange(new EnvironmentChangeEvent(Set.of("resilience.circuit-breaker.timeout")));

		assertThat(holder.current()).isSameAs(before);
	}

	private MockEnvironment validEnvironment() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("token.active-kid", "k1");
		environment.setProperty("token.public-keys.k1", encode(K1.getPublic().getEncoded()));
		environment.setProperty("token.private-keys.k1", encode(K1.getPrivate().getEncoded()));
		environment.setProperty("token.expiration-time", "24h");
		return environment;
	}

	private static String encode(byte[] der) {
		return Base64.getEncoder().encodeToString(der);
	}

	private static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			return generator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}
}
