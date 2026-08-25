package com.study.userservice.security;

import com.study.userservice.config.properties.TokenKeyProperties;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public record TokenKeys(
		String activeKid,
		PrivateKey activePrivateKey,
		Map<String, PublicKey> publicKeys,
		Duration expirationTime) {

	public TokenKeys {
		if (activeKid == null || activeKid.isBlank()) {
			throw new IllegalArgumentException("token.active-kid must be configured");
		}
		if (activePrivateKey == null) {
			throw new IllegalArgumentException("token.private-keys must contain the active kid: " + activeKid);
		}
		if (publicKeys == null || !publicKeys.containsKey(activeKid)) {
			throw new IllegalArgumentException("token.public-keys must contain the active kid: " + activeKid);
		}
		if (expirationTime == null || expirationTime.isNegative() || expirationTime.isZero()) {
			throw new IllegalArgumentException("token.expiration-time must be a positive duration");
		}
		publicKeys = Map.copyOf(publicKeys);
	}

	public static TokenKeys from(TokenKeyProperties properties) {
		if (properties.activeKid() == null || properties.privateKeys() == null
				|| !properties.privateKeys().containsKey(properties.activeKid())) {
			throw new IllegalArgumentException("token.private-keys must contain the active kid: " + properties.activeKid());
		}

		Map<String, PublicKey> publicKeys = new LinkedHashMap<>();
		if (properties.publicKeys() != null) {
			properties.publicKeys().forEach((kid, encoded) -> publicKeys.put(kid, parsePublicKey(kid, encoded)));
		}
		PrivateKey activePrivateKey = parsePrivateKey(properties.activeKid(),
				properties.privateKeys().get(properties.activeKid()));

		return new TokenKeys(properties.activeKid(), activePrivateKey, publicKeys, properties.expirationTime());
	}

	private static PublicKey parsePublicKey(String kid, String base64Der) {
		try {
			return KeyFactory.getInstance("RSA")
					.generatePublic(new X509EncodedKeySpec(Base64.getMimeDecoder().decode(base64Der)));
		} catch (Exception ex) {
			throw new IllegalArgumentException("token.public-keys." + kid + " is not a valid RSA public key", ex);
		}
	}

	private static PrivateKey parsePrivateKey(String kid, String base64Der) {
		try {
			return KeyFactory.getInstance("RSA")
					.generatePrivate(new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(base64Der)));
		} catch (Exception ex) {
			throw new IllegalArgumentException("token.private-keys." + kid + " is not a valid RSA private key", ex);
		}
	}
}
