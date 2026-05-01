package dev.hieplp.doxen.adapter.out.token;

import dev.hieplp.doxen.application.port.out.token.ValidateTokenPort;
import dev.hieplp.doxen.domain.constants.TokenConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidatorAdapter implements ValidateTokenPort {

    private final JwtDecoder decoder;

    @Override
    public Optional<TokenClaims> validate(String token) {
        try {
            final var jwt = decoder.decode(token);
            return Optional.of(new TokenClaims(jwt.getSubject(), jwt.getClaimAsString(TokenConstant.CLAIM_TYPE)));
        } catch (JwtException exception) {
            log.warn("Invalid JWT token. message={}", exception.getMessage());
            return Optional.empty();
        }
    }
}
