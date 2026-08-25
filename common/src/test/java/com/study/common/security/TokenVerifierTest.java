package com.study.common.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TokenVerifierTest {

    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final KeyPair OTHER_KEY_PAIR = generateKeyPair();
    private static final String KID = "k1";

    private final TokenVerifier verifier = new TokenVerifier(holderWith(KID, KEY_PAIR));

    @Test
    void 유효한_토큰이면_subject를_돌려준다() {
        String token = rs256Token(KID, KEY_PAIR, Instant.now().plusSeconds(3600));

        assertThat(verifier.verify(token)).contains("user-1");
    }

    @Test
    void 모르는_kid의_토큰이면_빈_값이다() {
        String token = rs256Token("no-such-kid", KEY_PAIR, Instant.now().plusSeconds(3600));

        assertThat(verifier.verify(token)).isEmpty();
    }

    @Test
    void 다른_개인키로_서명된_토큰이면_빈_값이다() {
        String token = rs256Token(KID, OTHER_KEY_PAIR, Instant.now().plusSeconds(3600));

        assertThat(verifier.verify(token)).isEmpty();
    }

    @Test
    void 만료된_토큰이면_빈_값이다() {
        String token = rs256Token(KID, KEY_PAIR, Instant.now().minusSeconds(60));

        assertThat(verifier.verify(token)).isEmpty();
    }

    @Test
    void 유효한_kid로_위장한_HS256_토큰이면_빈_값이다() {
        String token = Jwts.builder()
                .header().keyId(KID).and()
                .subject("user-1")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(
                        "attacker-chosen-symmetric-key-that-is-long-enough-for-hs256-attack!!".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(verifier.verify(token)).isEmpty();
    }

    @Test
    void 서명되지_않은_토큰이면_빈_값이다() {
        assertThat(verifier.verify("not-a-real-token")).isEmpty();
    }

    private static TokenKeyHolder holderWith(String kid, KeyPair keyPair) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("token.public-keys." + kid,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        return new TokenKeyHolder(environment);
    }

    private static String rs256Token(String kid, KeyPair keyPair, Instant expiry) {
        return Jwts.builder()
                .header().keyId(kid).and()
                .subject("user-1")
                .issuedAt(Date.from(Instant.now().minusSeconds(1)))
                .expiration(Date.from(expiry))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
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
