package dev.hieplp.doxen.adapter.in.api.dto.response.auth;

import java.time.Instant;

public record LoginWithPasswordResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
}
