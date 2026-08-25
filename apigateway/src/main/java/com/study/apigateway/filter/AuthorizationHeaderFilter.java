package com.study.apigateway.filter;

import com.study.common.security.TokenVerifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 엣지에서 JWT 유효성(authN)을 검증해 인증되지 않은 요청을 조기에 차단한다.
 * 신원은 원본 Authorization 헤더가 그대로 다운스트림에 전달되어, 각 서비스가 같은 토큰으로
 * 직접 검증·인가한다(방어 심층화). 게이트웨이는 별도 신원 헤더를 만들지 않는다.
 */
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private static final String BEARER_PREFIX = "Bearer ";

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
            if (tokenVerifier.verify(authorizationHeader.substring(BEARER_PREFIX.length())).isEmpty()) {
                return onError(exchange);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}
