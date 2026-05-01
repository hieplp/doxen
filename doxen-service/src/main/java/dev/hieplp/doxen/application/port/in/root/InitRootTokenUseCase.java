package dev.hieplp.doxen.application.port.in.root;

public interface InitRootTokenUseCase {

    InitRootTokenResult init(InitRootTokenCommand command);

    sealed interface InitRootTokenResult {
        record Success(String token) implements InitRootTokenResult {
        }

        record ExistedRoot() implements InitRootTokenResult {
        }
    }

    record InitRootTokenCommand() {
    }

}
