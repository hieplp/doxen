package dev.hieplp.doxen.adapter.in.runner;

import dev.hieplp.doxen.application.port.in.root.InitRootTokenUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitRootTokenRunner implements ApplicationRunner {

    private final InitRootTokenUseCase initRootTokenUseCase;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        final var command = new InitRootTokenUseCase.InitRootTokenCommand();
        final var result = initRootTokenUseCase.init(command);
        switch (result) {
            case InitRootTokenUseCase.InitRootTokenResult.Success success -> {
                System.out.printf("""
                        
                        ============================================================
                          ROOT TOKEN (use this to create the root account)
                        ============================================================
                          %s
                        ============================================================
                        %n""", success.token());
            }
            case InitRootTokenUseCase.InitRootTokenResult.ExistedRoot ignored -> {
                log.info("Root account already exists, skipping token initialization");
            }
        }
    }
}
