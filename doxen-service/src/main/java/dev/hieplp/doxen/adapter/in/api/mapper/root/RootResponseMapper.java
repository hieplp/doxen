package dev.hieplp.doxen.adapter.in.api.mapper.root;

import dev.hieplp.doxen.adapter.in.api.dto.response.root.CreateRootResponse;
import dev.hieplp.doxen.application.port.in.root.CreateRootUseCase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RootResponseMapper {
    CreateRootResponse toResponse(CreateRootUseCase.CreateRootResult.Success success);
}
