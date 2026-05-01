package dev.hieplp.doxen.adapter.out.persistence.jpa.mapper;

import dev.hieplp.doxen.adapter.out.persistence.jpa.entity.RootAccountEntity;
import dev.hieplp.doxen.domain.model.RootAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = VoJpaMapper.class)
public interface RootAccountJpaMapper {

    @Mapping(target = "userId", source = "userId")
    RootAccountEntity toEntity(RootAccount domain);

    @Mapping(target = "userId", source = "userId")
    RootAccount toDomain(RootAccountEntity entity);

}
