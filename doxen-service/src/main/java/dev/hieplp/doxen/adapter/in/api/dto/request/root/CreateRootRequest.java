package dev.hieplp.doxen.adapter.in.api.dto.request.root;

public record CreateRootRequest(
        String username,
        String password,
        String token
) {
}
