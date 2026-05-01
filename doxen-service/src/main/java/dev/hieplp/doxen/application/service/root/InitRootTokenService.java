package dev.hieplp.doxen.application.service.root.command;

import dev.hieplp.doxen.application.port.in.root.command.InitRootTokenUseCase;
import dev.hieplp.doxen.application.port.out.root.token.command.SaveRootTokenPort;
import dev.hieplp.doxen.application.port.out.root.account.command.ExistRootAccountPort;
import dev.hieplp.doxen.domain.constants.RootConstant;
import dev.hieplp.doxen.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitRootTokenService implements InitRootTokenUseCase {

    private final ExistRootAccountPort existRootAccountPort;
    private final SaveRootTokenPort saveRootTokenPort;

    @Override
    public InitRootTokenResult init(InitRootTokenCommand command) {
        log.info("Initializing root token");

        final var rootId = new UserId(RootConstant.ROOT_ID);

        if (existRootAccountPort.existsById(rootId)) {
            log.info("Root account already exists, skipping token initialization");
            return new InitRootTokenResult.ExistedRoot();
        }

        final var token = UUID.randomUUID().toString();
        saveRootTokenPort.save(token);

        log.info("Initialized root token successfully");
        return new InitRootTokenResult.Success(token);
    }
}
