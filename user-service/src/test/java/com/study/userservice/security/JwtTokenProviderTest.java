package com.study.userservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration EXPIRATION = Duration.ofHours(24);
    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final KeyPair OTHER_KEY_PAIR = generateKeyPair();

    @Test
    void 발급된_토큰의_subject는_사용자_식별자다() {
        JwtTokenProvider provider = providerAt(FIXED_NOW);

        String token = provider.createToken("user-1234");

        assertThat(provider.parseUserId(token)).isEqualTo("user-1234");
    }

    @Test
    void 발급된_토큰_헤더에는_활성_kid가_실린다() {
        JwtTokenProvider provider = providerAt(FIXED_NOW);

        String token = provider.createToken("user-1234");

        var header = Jwts.parser()
                .keyLocator(h -> KEY_PAIR.getPublic())
                .clock(() -> Date.from(FIXED_NOW))
                .build()
                .parseSignedClaims(token)
                .getHeader();
        assertThat(header.getKeyId()).isEqualTo("k1");
    }

    @Test
    void 유효기간이_지난_토큰은_만료로_거부된다() {
        String token = providerAt(FIXED_NOW).createToken("user-1234");
        JwtTokenProvider laterProvider = providerAt(FIXED_NOW.plus(EXPIRATION).plusSeconds(1));

        assertThatThrownBy(() -> laterProvider.parseUserId(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void 모르는_kid의_토큰은_거부된다() {
        JwtTokenProvider provider = providerAt(FIXED_NOW);
        String unknownKidToken = Jwts.builder()
                .header().keyId("no-such-kid").and()
                .subject("user-1234")
                .issuedAt(Date.from(FIXED_NOW))
                .expiration(Date.from(FIXED_NOW.plus(EXPIRATION)))
                .signWith(OTHER_KEY_PAIR.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThatThrownBy(() -> provider.parseUserId(unknownKidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void 같은_kid라도_다른_키로_서명된_토큰은_거부된다() {
        JwtTokenProvider provider = providerAt(FIXED_NOW);
        String forged = Jwts.builder()
                .header().keyId("k1").and()
                .subject("user-1234")
                .issuedAt(Date.from(FIXED_NOW))
                .expiration(Date.from(FIXED_NOW.plus(EXPIRATION)))
                .signWith(OTHER_KEY_PAIR.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThatThrownBy(() -> provider.parseUserId(forged))
                .isInstanceOf(JwtException.class);
    }

    private JwtTokenProvider providerAt(Instant now) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("token.active-kid", "k1");
        environment.setProperty("token.public-keys.k1", encode(KEY_PAIR.getPublic().getEncoded()));
        environment.setProperty("token.private-keys.k1", encode(KEY_PAIR.getPrivate().getEncoded()));
        environment.setProperty("token.expiration-time", "24h");

        return new JwtTokenProvider(new TokenKeyHolder(environment), Clock.fixed(now, ZoneOffset.UTC));
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
