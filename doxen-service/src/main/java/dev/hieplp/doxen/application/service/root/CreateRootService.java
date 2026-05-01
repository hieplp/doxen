package dev.hieplp.doxen.application.service.root;

import dev.hieplp.doxen.application.port.in.root.CreateRootUseCase;
import dev.hieplp.doxen.application.port.out.root.account.ExistRootAccountPort;
import dev.hieplp.doxen.application.port.out.root.account.SaveRootAccountPort;
import dev.hieplp.doxen.application.port.out.root.token.MatchRootTokenPort;
import dev.hieplp.doxen.application.port.out.security.HashPasswordPort;
import dev.hieplp.doxen.domain.constants.RootConstant;
import dev.hieplp.doxen.domain.model.RootAccount;
import dev.hieplp.doxen.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateRootService implements CreateRootUseCase {

    private final ExistRootAccountPort existRootAccountPort;
    private final MatchRootTokenPort matchRootTokenPort;
    private final SaveRootAccountPort saveRootAccountPort;
    private final HashPasswordPort hashPasswordPort;

    @Override
    public CreateRootResult create(CreateRootCommand command) {
        log.info("Creating root. username={}", command.username());

        final var rootId = new UserId(RootConstant.ROOT_ID);

        if (existRootAccountPort.existsById(rootId)) {
            log.warn("Root account already exists");
            return new CreateRootResult.AlreadyExists();
        }

        if (!matchRootTokenPort.matchAndConsume(command.token())) {
            log.warn("Invalid root token");
            return new CreateRootResult.InvalidToken();
        }

        saveRootAccountPort.save(RootAccount.builder()
                .userId(rootId)
                .username(command.username())
                .passwordHash(hashPasswordPort.hash(command.password()))
                .build());

        log.info("Created root successfully");
        return new CreateRootResult.Success(rootId.value());
    }
}
