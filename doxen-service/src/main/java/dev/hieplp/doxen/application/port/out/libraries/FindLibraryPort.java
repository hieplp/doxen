package dev.hieplp.doxen.application.port.out.libraries;

import dev.hieplp.doxen.common.exception.NotFoundException;
import dev.hieplp.doxen.domain.model.Library;
import dev.hieplp.doxen.domain.vo.LibraryId;

import java.util.Optional;

public interface FindLibraryPort {
    Optional<Library> findById(LibraryId libraryId);

    default Library getById(LibraryId libraryId) {
        return findById(libraryId).orElseThrow(NotFoundException::new);
    }
}
