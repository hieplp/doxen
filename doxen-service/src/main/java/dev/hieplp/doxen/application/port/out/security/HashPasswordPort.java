package dev.hieplp.doxen.application.port.out.security;

public interface HashPasswordPort {
    String hash(String rawPassword);
}
