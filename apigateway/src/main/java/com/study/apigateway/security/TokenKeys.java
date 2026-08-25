package com.study.apigateway.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public record TokenKeys(Map<String, PublicKey> publicKeys) {

    public TokenKeys {
        if (publicKeys == null || publicKeys.isEmpty()) {
            throw new IllegalArgumentException("token.public-keys must contain at least one key");
        }
        publicKeys = Map.copyOf(publicKeys);
    }

    public static TokenKeys from(Map<String, String> encodedPublicKeys) {
        Map<String, PublicKey> publicKeys = new LinkedHashMap<>();
        if (encodedPublicKeys != null) {
            encodedPublicKeys.forEach((kid, encoded) -> publicKeys.put(kid, parsePublicKey(kid, encoded)));
        }
        return new TokenKeys(publicKeys);
    }

    private static PublicKey parsePublicKey(String kid, String base64Der) {
        try {
            return KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getMimeDecoder().decode(base64Der)));
        } catch (Exception ex) {
            throw new IllegalArgumentException("token.public-keys." + kid + " is not a valid RSA public key", ex);
        }
    }
}
