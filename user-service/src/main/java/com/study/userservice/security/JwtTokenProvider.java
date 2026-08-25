package com.study.userservice.security;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

	private final TokenKeyHolder tokenKeyHolder;
	private final Clock clock;

	public JwtTokenProvider(TokenKeyHolder tokenKeyHolder, Clock clock) {
		this.tokenKeyHolder = tokenKeyHolder;
		this.clock = clock;
	}

	public String createToken(String userId) {
		TokenKeys keys = tokenKeyHolder.current();
		Instant now = clock.instant();

		return Jwts.builder()
				.header().keyId(keys.activeKid()).and()
				.subject(userId)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(keys.expirationTime())))
				.signWith(keys.activePrivateKey(), Jwts.SIG.RS256)
				.compact();
	}

	public String parseUserId(String token) {
		TokenKeys keys = tokenKeyHolder.current();

		return Jwts.parser()
				.keyLocator(header -> resolveKey(keys, header))
				.clock(() -> Date.from(clock.instant()))
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	private Key resolveKey(TokenKeys keys, io.jsonwebtoken.Header header) {
		if (header instanceof JwsHeader jwsHeader) {
			return keys.publicKeys().get(jwsHeader.getKeyId());
		}
		return null;
	}
}
