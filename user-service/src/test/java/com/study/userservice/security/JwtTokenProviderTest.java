package com.study.userservice.security;

import com.study.userservice.config.properties.TokenProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-only-jwt-signing-key-that-is-long-enough-for-hs512-0123456789abcd";
    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration EXPIRATION = Duration.ofHours(24);

    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @Test
    void 발급된_토큰의_subject는_사용자_식별자다() {
        JwtTokenProvider provider = providerAt(FIXED_NOW);

        String token = provider.createToken("user-1234");

        assertThat(parse(token).getPayload().getSubject()).isEqualTo("user-1234");
    }

    @Test
    void 발급된_토큰의_만료시각은_현재시각에_설정된_유효기간을_더한_값이다() {
        JwtTokenProvider provider = providerAt(FIXED_NOW);

        String token = provider.createToken("user-1234");

        assertThat(parse(token).getPayload().getExpiration())
                .isEqualTo(Date.from(FIXED_NOW.plus(EXPIRATION)));
    }

    @Test
    void 유효기간이_지난_시점에_검증하면_만료로_판정된다() {
        String token = providerAt(FIXED_NOW).createToken("user-1234");
        Clock afterExpiry = Clock.fixed(FIXED_NOW.plus(EXPIRATION).plusSeconds(1), ZoneOffset.UTC);

        assertThatThrownBy(() -> Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(afterExpiry.instant()))
                .build()
                .parseSignedClaims(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void 서명키가_512비트에_못_미치면_설정_바인딩_단계에서_거부된다() {
        assertThatThrownBy(() -> new TokenProperties("too-short", EXPIRATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("token.secret");
    }

    @Test
    void 발급한_토큰을_파싱하면_사용자_식별자를_돌려준다() {
        JwtTokenProvider provider = providerAt(FIXED_NOW);

        String token = provider.createToken("user-1234");

        assertThat(provider.parseUserId(token)).isEqualTo("user-1234");
    }

    @Test
    void 만료된_토큰을_파싱하면_거부된다() {
        String token = providerAt(FIXED_NOW).createToken("user-1234");
        JwtTokenProvider laterProvider = providerAt(FIXED_NOW.plus(EXPIRATION).plusSeconds(1));

        assertThatThrownBy(() -> laterProvider.parseUserId(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    private JwtTokenProvider providerAt(Instant now) {
        return new JwtTokenProvider(new TokenProperties(SECRET, EXPIRATION),
                Clock.fixed(now, ZoneOffset.UTC));
    }

    private io.jsonwebtoken.Jws<io.jsonwebtoken.Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(FIXED_NOW))
                .build()
                .parseSignedClaims(token);
    }
}
