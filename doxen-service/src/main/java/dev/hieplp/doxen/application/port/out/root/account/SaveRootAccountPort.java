package dev.hieplp.doxen.application.port.out.root.account;

import dev.hieplp.doxen.domain.model.RootAccount;

public interface SaveRootAccountPort {
    RootAccount save(RootAccount rootAccount);
}
