package dev.hieplp.doxen.application.port.out.root.account;

import dev.hieplp.doxen.domain.model.RootAccount;

import java.util.Optional;

public interface LoadRootAccountPort {
    Optional<RootAccount> findByUsername(String username);
}
