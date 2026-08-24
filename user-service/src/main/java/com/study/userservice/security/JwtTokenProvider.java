package com.study.userservice.security;

import com.study.userservice.config.properties.TokenProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

	private final SecretKey secretKey;
	private final TokenProperties tokenProperties;
	private final Clock clock;

	public JwtTokenProvider(TokenProperties tokenProperties, Clock clock) {
		this.secretKey = Keys.hmacShaKeyFor(tokenProperties.secret().getBytes(StandardCharsets.UTF_8));
		this.tokenProperties = tokenProperties;
		this.clock = clock;
	}

	public String createToken(String userId) {
		Instant now = clock.instant();

		return Jwts.builder()
				.subject(userId)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(tokenProperties.expirationTime())))
				.signWith(secretKey)
				.compact();
	}

	public String parseUserId(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.clock(() -> Date.from(clock.instant()))
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}
}
