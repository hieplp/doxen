package dev.hieplp.doxen.adapter.out.token;

import dev.hieplp.doxen.application.port.out.token.GenerateTokenPairPort;
import dev.hieplp.doxen.config.properties.RsaKeyProperties;
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
public class JwtTokenAdapter implements GenerateTokenPairPort {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final RsaKeyProperties keys;

    @Override
    public TokenPair generate(UserId userId) {
        final var now = Instant.now();
        final var accessExpiresAt = now.plusSeconds(keys.accessTokenExpiry());
        final var refreshExpiresAt = now.plusSeconds(keys.refreshTokenExpiry());
        return new TokenPair(
                buildToken(userId, now, accessExpiresAt, TYPE_ACCESS),
                accessExpiresAt,
                buildToken(userId, now, refreshExpiresAt, TYPE_REFRESH),
                refreshExpiresAt
        );
    }

    private String buildToken(UserId userId, Instant now, Instant expiresAt, String type) {
        final var claims = JwtClaimsSet.builder()
                .issuer(keys.issuer())
                .subject(userId.value())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(CLAIM_TYPE, type)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(() -> "RS256").build(), claims)).getTokenValue();
    }
}
