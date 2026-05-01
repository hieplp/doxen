package dev.hieplp.doxen.adapter.in.api.controller;

import dev.hieplp.doxen.adapter.in.api.dto.request.auth.LoginWithPasswordRequest;
import dev.hieplp.doxen.adapter.in.api.dto.response.auth.LoginWithPasswordResponse;
import dev.hieplp.doxen.adapter.in.api.dto.response.base.ApiResponse;
import dev.hieplp.doxen.adapter.in.api.mapper.auth.AuthRequestMapper;
import dev.hieplp.doxen.adapter.in.api.mapper.auth.AuthResponseMapper;
import dev.hieplp.doxen.adapter.in.api.statuscode.ErrorCode;
import dev.hieplp.doxen.application.port.in.auth.LoginWithPasswordUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    // MapStruct
    private final AuthRequestMapper requestMapper;
    private final AuthResponseMapper responseMapper;

    // UseCase
    private final LoginWithPasswordUseCase loginWithPasswordUseCase;

    @PostMapping("/login")
    public ApiResponse<LoginWithPasswordResponse> loginWithPassword(@RequestBody LoginWithPasswordRequest request) {
        log.info("Login with password. username={}", request.username());
        final var command = requestMapper.toCommand(request);
        final var result = loginWithPasswordUseCase.login(command);
        return switch (result) {
            case LoginWithPasswordUseCase.LoginWithPasswordResult.Success success -> {
                log.info("Login successful. username={}", request.username());
                yield ApiResponse.success(responseMapper.toResponse(success));
            }
            case LoginWithPasswordUseCase.LoginWithPasswordResult.InvalidCredentials ignored -> {
                log.warn("Invalid credentials. username={}", request.username());
                yield new ApiResponse<>(ErrorCode.INVALID_CREDENTIALS);
            }
        };
    }
}
