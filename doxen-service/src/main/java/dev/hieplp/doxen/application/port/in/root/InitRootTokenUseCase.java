package dev.hieplp.doxen.application.port.in.root.command;

public interface InitRootTokenUseCase {

    InitRootTokenResult init(InitRootTokenCommand command);

    record InitRootTokenCommand() {
    }

    sealed interface InitRootTokenResult {
        record Success(String token) implements InitRootTokenResult {
        }

        record ExistedRoot() implements InitRootTokenResult {
        }
    }

}
