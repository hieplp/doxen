package dev.hieplp.doxen.application.port.out.libraries;

import dev.hieplp.doxen.domain.vo.LibraryId;

public interface ExistLibraryPort {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndLibraryIdNot(String slug, LibraryId libraryId);
}
