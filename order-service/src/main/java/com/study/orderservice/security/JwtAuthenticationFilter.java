package com.study.orderservice.security;

import com.study.common.security.TokenVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Bearer 토큰을 공개키로 검증(authN)해 인증 신원을 SecurityContext 에 세운다.
 * order-service 는 게이트웨이 헤더를 신뢰하지 않고 토큰을 직접 검증하므로,
 * 게이트웨이를 우회한 직접 호출도 유효한 서명 없이는 통과하지 못한다.
 * 소유권 판정(authZ)은 세워진 신원을 근거로 컨트롤러의 {@code @PreAuthorize} 가 담당한다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenVerifier tokenVerifier;

    public JwtAuthenticationFilter(TokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            tokenVerifier.verify(header.substring(BEARER_PREFIX.length()))
                    .ifPresent(userId -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(userId, null, List.of())));
        }

        filterChain.doFilter(request, response);
    }
}
