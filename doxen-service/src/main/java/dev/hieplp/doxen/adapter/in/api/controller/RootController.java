package dev.hieplp.doxen.adapter.in.api.controller;

import dev.hieplp.doxen.adapter.in.api.dto.request.root.CreateRootRequest;
import dev.hieplp.doxen.adapter.in.api.dto.response.base.ApiResponse;
import dev.hieplp.doxen.adapter.in.api.dto.response.root.CreateRootResponse;
import dev.hieplp.doxen.adapter.in.api.mapper.root.RootRequestMapper;
import dev.hieplp.doxen.adapter.in.api.mapper.root.RootResponseMapper;
import dev.hieplp.doxen.adapter.in.api.statuscode.ErrorCode;
import dev.hieplp.doxen.application.port.in.root.CreateRootUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/root")
@RequiredArgsConstructor
public class RootController {

    // MapStruct
    private final RootRequestMapper requestMapper;
    private final RootResponseMapper responseMapper;

    // UseCase
    private final CreateRootUseCase createRootUseCase;

    @PostMapping
    public ApiResponse<CreateRootResponse> createRoot(@RequestBody CreateRootRequest request) {
        log.info("Creating root. username={}", request.username());
        final var command = requestMapper.toCommand(request);
        final var result = createRootUseCase.create(command);
        return switch (result) {
            case CreateRootUseCase.CreateRootResult.Success success -> {
                log.info("Created root successfully. userId={}", success.userId());
                yield ApiResponse.success(responseMapper.toResponse(success));
            }
            case CreateRootUseCase.CreateRootResult.InvalidToken ignored -> {
                log.warn("Invalid root token");
                yield new ApiResponse<>(ErrorCode.INVALID_TOKEN);
            }
            case CreateRootUseCase.CreateRootResult.AlreadyExists ignored -> {
                log.warn("Root account already exists");
                yield new ApiResponse<>(ErrorCode.ROOT_ALREADY_EXISTS);
            }
        };
    }

}
