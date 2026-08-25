package com.study.apigateway.security;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Optional;


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
