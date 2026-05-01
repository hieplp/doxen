package dev.hieplp.doxen.application.port.out.security;

public interface MatchPasswordPort {
    boolean matches(String rawPassword, String encodedPassword);
}
