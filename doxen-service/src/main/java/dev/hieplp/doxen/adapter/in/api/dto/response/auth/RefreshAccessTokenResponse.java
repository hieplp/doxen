package dev.hieplp.doxen.adapter.in.api.dto.response.auth;

import java.time.Instant;

public record RefreshAccessTokenResponse(
        String accessToken,
        Instant accessTokenExpiresAt
) {
}
