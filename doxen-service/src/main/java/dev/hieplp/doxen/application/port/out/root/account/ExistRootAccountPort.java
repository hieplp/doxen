package dev.hieplp.doxen.application.port.out.root.account.command;

import dev.hieplp.doxen.domain.vo.UserId;

public interface ExistRootAccountPort {
    boolean existsById(UserId userId);
}
