package dev.hieplp.doxen.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class AuditModel {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
