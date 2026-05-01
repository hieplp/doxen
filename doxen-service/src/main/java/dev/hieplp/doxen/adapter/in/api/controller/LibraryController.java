package dev.hieplp.doxen.adapter.in.api.controller;

import dev.hieplp.doxen.adapter.in.api.dto.request.libraries.CreateLibraryRequest;
import dev.hieplp.doxen.adapter.in.api.dto.request.libraries.UpdateLibraryRequest;
import dev.hieplp.doxen.adapter.in.api.dto.response.base.ApiResponse;
import dev.hieplp.doxen.adapter.in.api.dto.response.libraries.CreateLibraryResponse;
import dev.hieplp.doxen.adapter.in.api.dto.response.libraries.UpdateLibraryResponse;
import dev.hieplp.doxen.adapter.in.api.mapper.libraries.LibraryRequestMapper;
import dev.hieplp.doxen.adapter.in.api.mapper.libraries.LibraryResponseMapper;
import dev.hieplp.doxen.adapter.in.api.statuscode.ErrorCode;
import dev.hieplp.doxen.application.port.in.libraries.CreateLibraryUseCase;
import dev.hieplp.doxen.application.port.in.libraries.UpdateLibraryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/libraries")
@RequiredArgsConstructor
public class LibraryController {

    // MapStruct
    private final LibraryRequestMapper requestMapper;
    private final LibraryResponseMapper responseMapper;

    // UseCase
    private final CreateLibraryUseCase createLibraryUseCase;
    private final UpdateLibraryUseCase updateLibraryUseCase;

    @PostMapping
    public ApiResponse<CreateLibraryResponse> createLibrary(@Valid @RequestBody CreateLibraryRequest request) {
        log.info("Creating library. name={}", request.name());
        final var command = requestMapper.toCommand(request);
        final var result = createLibraryUseCase.create(command);
        return switch (result) {
            case CreateLibraryUseCase.CreateLibraryResult.Success success -> {
                log.info("Created library successfully. libraryId={}", success.libraryId());
                yield ApiResponse.success(responseMapper.toResponse(success));
            }
            case CreateLibraryUseCase.CreateLibraryResult.DuplicateSlug ignored -> {
                log.warn("Duplicate slug. name={}", request.name());
                yield new ApiResponse<>(ErrorCode.LIBRARY_DUPLICATE_SLUG);
            }
        };
    }

    @PutMapping("/{libraryId}")
    public ApiResponse<UpdateLibraryResponse> updateLibrary(
            @PathVariable String libraryId,
            @Valid @RequestBody UpdateLibraryRequest request
    ) {
        log.info("Updating library. libraryId={}", libraryId);
        final var command = requestMapper.toCommand(libraryId, request);
        final var result = updateLibraryUseCase.update(command);
        return switch (result) {
            case UpdateLibraryUseCase.UpdateLibraryResult.Success success -> {
                log.info("Updated library successfully. libraryId={}", success.libraryId());
                yield ApiResponse.success(responseMapper.toResponse(success));
            }
            case UpdateLibraryUseCase.UpdateLibraryResult.DuplicateSlug ignored -> {
                log.warn("Duplicate slug. name={}", request.name());
                yield new ApiResponse<>(ErrorCode.LIBRARY_DUPLICATE_SLUG);
            }
        };
    }

}
