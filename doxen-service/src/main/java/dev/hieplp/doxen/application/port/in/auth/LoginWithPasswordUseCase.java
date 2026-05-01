package dev.hieplp.doxen.application.port.in.auth;

import java.time.Instant;

public interface LoginWithPasswordUseCase {

    LoginWithPasswordResult login(LoginWithPasswordCommand command);

    sealed interface LoginWithPasswordResult {
        record Success(
                String accessToken,
                Instant accessTokenExpiresAt,
                String refreshToken,
                Instant refreshTokenExpiresAt
        ) implements LoginWithPasswordResult {
        }

        record InvalidCredentials() implements LoginWithPasswordResult {
        }
    }

    record LoginWithPasswordCommand(
            String username,
            String password
    ) {
    }
}
