package com.study.apigateway.filter;

import com.study.common.security.TokenKeyHolder;
import com.study.common.security.TokenVerifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationHeaderFilterTest {

    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final KeyPair OTHER_KEY_PAIR = generateKeyPair();
    private static final String KID = "k1";

    private final GatewayFilter filter = buildFilter();
    private final AtomicBoolean chained = new AtomicBoolean();
    private final GatewayFilterChain chain = exchange -> {
        chained.set(true);
        return Mono.empty();
    };

    @Test
    void 인증_헤더가_없으면_401로_거부하고_라우팅하지_않는다() {
        MockServerWebExchange exchange = exchangeWith(null);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chained).isFalse();
    }

    @Test
    void 다른_개인키로_서명된_토큰은_401로_거부한다() {
        String forged = rs256Token(KID, OTHER_KEY_PAIR, Instant.now().plusSeconds(3600));
        MockServerWebExchange exchange = exchangeWith("Bearer " + forged);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chained).isFalse();
    }

    @Test
    void 모르는_kid의_토큰은_401로_거부한다() {
        String unknownKid = rs256Token("no-such-kid", KEY_PAIR, Instant.now().plusSeconds(3600));
        MockServerWebExchange exchange = exchangeWith("Bearer " + unknownKid);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chained).isFalse();
    }

    @Test
    void 만료된_토큰은_401로_거부한다() {
        String expired = rs256Token(KID, KEY_PAIR, Instant.now().minusSeconds(60));
        MockServerWebExchange exchange = exchangeWith("Bearer " + expired);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chained).isFalse();
    }

    @Test
    void 유효한_kid로_위장한_HS256_토큰은_401로_거부한다() {
        String algConfusion = Jwts.builder()
                .header().keyId(KID).and()
                .subject("user-1")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(
                        "attacker-chosen-symmetric-key-that-is-long-enough-for-hs256-attack!!".getBytes(StandardCharsets.UTF_8)))
                .compact();
        MockServerWebExchange exchange = exchangeWith("Bearer " + algConfusion);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chained).isFalse();
    }

    @Test
    void 유효한_토큰은_다음_필터로_통과시킨다() {
        String valid = rs256Token(KID, KEY_PAIR, Instant.now().plusSeconds(3600));
        MockServerWebExchange exchange = exchangeWith("Bearer " + valid);

        filter.filter(exchange, chain).block();

        assertThat(chained).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static GatewayFilter buildFilter() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("token.public-keys." + KID,
                Base64.getEncoder().encodeToString(KEY_PAIR.getPublic().getEncoded()));

        return new AuthorizationHeaderFilter(new TokenVerifier(new TokenKeyHolder(environment)))
                .apply(new AuthorizationHeaderFilter.Config());
    }

    private MockServerWebExchange exchangeWith(String authorizationHeader) {
        MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get("/orders");
        if (authorizationHeader != null) {
            request.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        return MockServerWebExchange.from(request.build());
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
