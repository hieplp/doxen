package dev.hieplp.doxen.application.service.auth;

import dev.hieplp.doxen.application.port.in.auth.RefreshAccessTokenUseCase;
import dev.hieplp.doxen.application.port.out.token.GenerateAccessTokenPort;
import dev.hieplp.doxen.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final GenerateAccessTokenPort generateAccessTokenPort;

    @Override
    public RefreshAccessTokenResult refresh(RefreshAccessTokenCommand command) {
        log.info("Refreshing access token. userId={}", command.userId());
        final var accessToken = generateAccessTokenPort.generateAccessToken(new UserId(command.userId()));
        return new RefreshAccessTokenResult(accessToken.accessToken(), accessToken.accessTokenExpiresAt());
    }
}
