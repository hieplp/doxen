package dev.hieplp.doxen.application.port.out.root.account;

import dev.hieplp.doxen.domain.vo.UserId;

public interface ExistRootAccountPort {
    boolean existsById(UserId userId);
}
