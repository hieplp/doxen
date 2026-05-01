package dev.hieplp.doxen.application.port.in.root;

public interface CreateRootUseCase {

    CreateRootResult create(CreateRootCommand command);

    sealed interface CreateRootResult {
        record Success(String userId) implements CreateRootResult {
        }

        record InvalidToken() implements CreateRootResult {
        }

        record AlreadyExists() implements CreateRootResult {
        }
    }

    record CreateRootCommand(
            String username,
            String password,
            String token
    ) {
    }

}
