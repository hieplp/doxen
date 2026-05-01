package dev.hieplp.doxen.adapter.out.persistence.jpa.mapper;

import dev.hieplp.doxen.adapter.out.persistence.jpa.entity.LibraryEntity;
import dev.hieplp.doxen.domain.model.Library;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = VoJpaMapper.class)
public interface LibraryJpaMapper {

    LibraryEntity toEntity(Library domain);

    Library toDomain(LibraryEntity entity);
}
