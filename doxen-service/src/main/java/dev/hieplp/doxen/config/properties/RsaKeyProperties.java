package dev.hieplp.doxen.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "doxen.security.jwt")
public record RsaKeyProperties(
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey,
        long accessTokenExpiry,
        long refreshTokenExpiry,
        String issuer
) {
}
