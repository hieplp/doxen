package dev.hieplp.doxen.adapter.in.api.mapper.libraries;

import dev.hieplp.doxen.adapter.in.api.dto.response.libraries.CreateLibraryResponse;
import dev.hieplp.doxen.adapter.in.api.dto.response.libraries.UpdateLibraryResponse;
import dev.hieplp.doxen.application.port.in.libraries.CreateLibraryUseCase;
import dev.hieplp.doxen.application.port.in.libraries.UpdateLibraryUseCase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LibraryResponseMapper {
    CreateLibraryResponse toResponse(CreateLibraryUseCase.CreateLibraryResult.Success success);

    UpdateLibraryResponse toResponse(UpdateLibraryUseCase.UpdateLibraryResult.Success success);
}
