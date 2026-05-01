package dev.hieplp.doxen.application.port.out.token;

import dev.hieplp.doxen.domain.vo.UserId;

import java.time.Instant;

public interface GenerateAccessTokenPort {
    AccessToken generateAccessToken(UserId userId);

    record AccessToken(
            String accessToken,
            Instant accessTokenExpiresAt
    ) {
    }
}
