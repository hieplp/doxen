package dev.hieplp.doxen.application.port.in.auth;

import java.time.Instant;

public interface RefreshAccessTokenUseCase {

    RefreshAccessTokenResult refresh(RefreshAccessTokenCommand command);

    record RefreshAccessTokenResult(
            String accessToken,
            Instant accessTokenExpiresAt
    ) {
    }

    record RefreshAccessTokenCommand(String userId) {
    }
}
