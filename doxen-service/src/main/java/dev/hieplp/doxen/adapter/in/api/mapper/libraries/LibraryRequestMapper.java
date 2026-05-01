package dev.hieplp.doxen.adapter.in.api.mapper.libraries;

import dev.hieplp.doxen.adapter.in.api.dto.request.libraries.CreateLibraryRequest;
import dev.hieplp.doxen.adapter.in.api.dto.request.libraries.UpdateLibraryRequest;
import dev.hieplp.doxen.application.port.in.libraries.CreateLibraryUseCase;
import dev.hieplp.doxen.application.port.in.libraries.UpdateLibraryUseCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LibraryRequestMapper {
    CreateLibraryUseCase.CreateLibraryCommand toCommand(CreateLibraryRequest request);

    @Mapping(target = "libraryId", source = "libraryId")
    UpdateLibraryUseCase.UpdateLibraryCommand toCommand(String libraryId, UpdateLibraryRequest request);
}
