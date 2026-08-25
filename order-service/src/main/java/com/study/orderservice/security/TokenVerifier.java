package com.study.orderservice.security;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Optional;

/**
 * JWT 검증 관심사를 한곳에 격리한다. jjwt 의존은 이 클래스에만 두고,
 * 키 스냅샷({@link TokenKeys})은 순수 java.security 타입만 다룬다.
 * 검증에 성공하면 subject 를, 실패하면 빈 값을 돌려준다(예외를 흘리지 않는다).
 */
@Component
public class TokenVerifier {

    private final TokenKeyHolder tokenKeyHolder;

    public TokenVerifier(TokenKeyHolder tokenKeyHolder) {
        this.tokenKeyHolder = tokenKeyHolder;
    }

    public Optional<String> verify(String jwt) {
        TokenKeys keys = tokenKeyHolder.current();
        try {
            String subject = Jwts.parser()
                    .keyLocator(header -> resolveKey(keys, header))
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .getSubject();

            return (subject == null || subject.isBlank()) ? Optional.empty() : Optional.of(subject);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Key resolveKey(TokenKeys keys, Header header) {
        if (header instanceof JwsHeader jwsHeader) {
            return keys.publicKeys().get(jwsHeader.getKeyId());
        }
        return null;
    }
}
