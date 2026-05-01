package dev.hieplp.doxen.adapter.in.api.mapper.root;

import dev.hieplp.doxen.adapter.in.api.dto.request.root.CreateRootRequest;
import dev.hieplp.doxen.application.port.in.root.CreateRootUseCase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RootRequestMapper {
    CreateRootUseCase.CreateRootCommand toCommand(CreateRootRequest request);
}
