package dev.hieplp.doxen.application.port.out.token;

import dev.hieplp.doxen.domain.vo.UserId;

import java.time.Instant;

public interface GenerateTokenPairPort {
    TokenPair generate(UserId userId);

    record TokenPair(
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt
    ) {
    }
}
