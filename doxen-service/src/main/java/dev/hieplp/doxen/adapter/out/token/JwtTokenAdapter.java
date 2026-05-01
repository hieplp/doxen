package dev.hieplp.doxen.adapter.out.token;

import dev.hieplp.doxen.application.port.out.token.GenerateAccessTokenPort;
import dev.hieplp.doxen.application.port.out.token.GenerateTokenPairPort;
import dev.hieplp.doxen.config.properties.RsaKeyProperties;
import dev.hieplp.doxen.domain.constants.TokenConstant;
import dev.hieplp.doxen.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements GenerateTokenPairPort, GenerateAccessTokenPort {

    private final JwtEncoder jwtEncoder;
    private final RsaKeyProperties keys;

    @Override
    public AccessToken generateAccessToken(UserId userId) {
        final var now = Instant.now();
        final var accessExpiresAt = now.plusSeconds(keys.accessTokenExpiry());
        return new AccessToken(
                buildToken(userId, now, accessExpiresAt, TokenConstant.ACCESS_TOKEN_TYPE),
                accessExpiresAt
        );
    }

    @Override
    public TokenPair generate(UserId userId) {
        final var now = Instant.now();
        final var accessExpiresAt = now.plusSeconds(keys.accessTokenExpiry());
        final var refreshExpiresAt = now.plusSeconds(keys.refreshTokenExpiry());
        return new TokenPair(
                buildToken(userId, now, accessExpiresAt, TokenConstant.ACCESS_TOKEN_TYPE),
                accessExpiresAt,
                buildToken(userId, now, refreshExpiresAt, TokenConstant.REFRESH_TOKEN_TYPE),
                refreshExpiresAt
        );
    }

    private String buildToken(UserId userId, Instant now, Instant expiresAt, String type) {
        final var claims = JwtClaimsSet.builder()
                .issuer(keys.issuer())
                .subject(userId.value())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(TokenConstant.CLAIM_TYPE, type)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(() -> "RS256").build(), claims)).getTokenValue();
    }
}
