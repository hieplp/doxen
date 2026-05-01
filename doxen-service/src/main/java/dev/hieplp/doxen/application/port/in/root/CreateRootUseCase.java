package dev.hieplp.doxen.application.port.in.root.command;

public interface CreateRootUseCase {

    CreateRootResult create(CreateRootCommand command);

    record CreateRootCommand(
            String username,
            String password,
            String token
    ) {
    }

    sealed interface CreateRootResult {
        record Success(String userId) implements CreateRootResult {}
        record InvalidToken() implements CreateRootResult {}
        record AlreadyExists() implements CreateRootResult {}
    }

}
