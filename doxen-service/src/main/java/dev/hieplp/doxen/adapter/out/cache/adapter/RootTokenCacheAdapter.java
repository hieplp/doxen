package dev.hieplp.doxen.adapter.out.cache.adapter;

import dev.hieplp.doxen.application.port.out.root.token.GetRootTokenPort;
import dev.hieplp.doxen.application.port.out.root.token.MatchRootTokenPort;
import dev.hieplp.doxen.application.port.out.root.token.SaveRootTokenPort;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RootTokenCacheAdapter implements SaveRootTokenPort, GetRootTokenPort, MatchRootTokenPort {

    private final AtomicReference<String> cachedToken = new AtomicReference<>();

    @Override
    public void save(String token) {
        cachedToken.set(token);
    }

    @Override
    public Optional<String> get() {
        return Optional.ofNullable(cachedToken.get());
    }

    @Override
    public boolean matchAndConsume(String token) {
        var currentToken = cachedToken.get();

        if (!Objects.equals(currentToken, token)) {
            return false;
        }

        return cachedToken.compareAndSet(currentToken, null);
    }
}
