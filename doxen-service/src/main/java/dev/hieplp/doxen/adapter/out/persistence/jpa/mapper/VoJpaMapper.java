package dev.hieplp.doxen.adapter.out.persistence.jpa.mapper;

import dev.hieplp.doxen.domain.vo.UserId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoJpaMapper {

    // UserId
    default String fromUserId(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UserId toUserId(String value) {
        return value == null ? null : new UserId(value);
    }

}
