package dev.hieplp.doxen.domain.model;

import dev.hieplp.doxen.domain.vo.UserId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class RootAccount extends AuditModel {
    private UserId userId;
    private String username;
    private String passwordHash;
}
