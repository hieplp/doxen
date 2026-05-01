package dev.hieplp.doxen.adapter.in.api.dto.request.auth;

public record LoginWithPasswordRequest(
        String username,
        String password
) {
}
