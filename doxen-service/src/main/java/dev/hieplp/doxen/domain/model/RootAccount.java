package dev.hieplp.doxen.domain.model;

import dev.hieplp.doxen.domain.vo.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RootAccount {
    private UserId userId;
    private String username;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
