package dev.hieplp.doxen.application.port.out.root.token;

public interface MatchRootTokenPort {
    boolean matchAndConsume(String token);
}
