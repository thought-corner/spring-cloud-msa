package com.study.apigateway.filter;

import com.study.apigateway.security.TokenVerifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private static final String BEARER_PREFIX = "Bearer ";

    public static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";

    private final TokenVerifier tokenVerifier;

    public AuthorizationHeaderFilter(TokenVerifier tokenVerifier) {
        super(Config.class);
        this.tokenVerifier = tokenVerifier;
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

            return tokenVerifier.verify(authorizationHeader.substring(BEARER_PREFIX.length()))
                    .map(subject -> chain.filter(withAuthenticatedUser(exchange, subject)))
                    .orElseGet(() -> onError(exchange));
        };
    }

    private ServerWebExchange withAuthenticatedUser(ServerWebExchange exchange, String subject) {
        return exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(AUTHENTICATED_USER_HEADER, subject)))
                .build();
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}
