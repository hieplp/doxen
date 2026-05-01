package dev.hieplp.doxen.adapter.in.api.mapper.auth;

import dev.hieplp.doxen.adapter.in.api.dto.request.auth.LoginWithPasswordRequest;
import dev.hieplp.doxen.application.port.in.auth.LoginWithPasswordUseCase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthRequestMapper {
    LoginWithPasswordUseCase.LoginWithPasswordCommand toCommand(LoginWithPasswordRequest request);
}
