package dev.hieplp.doxen.application.port.out.token;

import java.util.Optional;

public interface ValidateTokenPort {
    Optional<TokenClaims> validate(String token);

    record TokenClaims(
            String subject,
            String type
    ) {
    }
}
