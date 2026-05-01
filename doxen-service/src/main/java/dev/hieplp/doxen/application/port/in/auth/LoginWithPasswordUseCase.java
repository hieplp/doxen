package dev.hieplp.doxen.application.port.in.auth.command;

public interface LoginWithPasswordUseCase {

    LoginWithPasswordResult login(LoginWithPasswordCommand command);

    record LoginWithPasswordCommand(
            String username,
            String password
    ) {
    }

    sealed interface LoginWithPasswordResult {
        record Success() implements LoginWithPasswordResult {
        }
    }
}
