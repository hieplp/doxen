package dev.hieplp.doxen.domain.model;

import dev.hieplp.doxen.domain.enums.LibraryStatus;
import dev.hieplp.doxen.domain.vo.LibraryId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Library extends AuditModel {
    private LibraryId libraryId;
    private String slug;
    private String name;
    private String description;
    private String homepageUrl;
    private LibraryStatus status;
    private LocalDateTime deletedAt;
}
