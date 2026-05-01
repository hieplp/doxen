package dev.hieplp.doxen.application.service.auth;

import dev.hieplp.doxen.application.port.in.auth.LoginWithPasswordUseCase;
import dev.hieplp.doxen.application.port.out.root.account.LoadRootAccountPort;
import dev.hieplp.doxen.application.port.out.security.MatchPasswordPort;
import dev.hieplp.doxen.application.port.out.token.GenerateTokenPairPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginWithPasswordService implements LoginWithPasswordUseCase {

    private final LoadRootAccountPort loadRootAccountPort;
    private final MatchPasswordPort matchPasswordPort;
    private final GenerateTokenPairPort generateTokenPairPort;

    @Override
    public LoginWithPasswordResult login(LoginWithPasswordCommand command) {
        log.info("Login with password. username={}", command.username());

        final var optionalAccount = loadRootAccountPort.findByUsername(command.username());
        if (optionalAccount.isEmpty()) {
            log.warn("Account not found. username={}", command.username());
            return new LoginWithPasswordResult.InvalidCredentials();
        }

        final var account = optionalAccount.get();
        final var userId = account.getUserId();

        if (!matchPasswordPort.matches(command.password(), account.getPasswordHash())) {
            log.warn("Invalid password. userId={}", userId);
            return new LoginWithPasswordResult.InvalidCredentials();
        }

        final var tokenPair = generateTokenPairPort.generate(userId);

        log.info("Login successful. userId={}", userId);
        return new LoginWithPasswordResult.Success(
                tokenPair.accessToken(),
                tokenPair.accessTokenExpiresAt(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenExpiresAt()
        );
    }
}
