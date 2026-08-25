package com.study.apigateway.filter;

import com.study.apigateway.security.TokenKeyHolder;
import com.study.apigateway.security.TokenKeys;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenKeyHolder tokenKeyHolder;

    public AuthorizationHeaderFilter(TokenKeyHolder tokenKeyHolder) {
        super(Config.class);
        this.tokenKeyHolder = tokenKeyHolder;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
                return onError(exchange);
            }
            if (!isJwtValid(authorizationHeader.substring(BEARER_PREFIX.length()))) {
                return onError(exchange);
            }

            return chain.filter(exchange);
        };
    }

    private boolean isJwtValid(String jwt) {
        TokenKeys keys = tokenKeyHolder.current();
        try {
            String subject = Jwts.parser()
                    .keyLocator(header -> resolveKey(keys, header))
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .getSubject();

            return subject != null && !subject.isBlank();
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Key resolveKey(TokenKeys keys, io.jsonwebtoken.Header header) {
        if (header instanceof JwsHeader jwsHeader) {
            return keys.publicKeys().get(jwsHeader.getKeyId());
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        return response.setComplete();
    }
}
