package dev.hieplp.doxen.application.port.out.root.token.command;

public interface MatchRootTokenPort {
    boolean matchAndConsume(String token);
}
