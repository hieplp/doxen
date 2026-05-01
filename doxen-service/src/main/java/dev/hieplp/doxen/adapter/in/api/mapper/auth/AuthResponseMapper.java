package dev.hieplp.doxen.adapter.in.api.mapper.auth;

import dev.hieplp.doxen.adapter.in.api.dto.response.auth.LoginWithPasswordResponse;
import dev.hieplp.doxen.application.port.in.auth.LoginWithPasswordUseCase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthResponseMapper {
    LoginWithPasswordResponse toResponse(LoginWithPasswordUseCase.LoginWithPasswordResult.Success success);
}
