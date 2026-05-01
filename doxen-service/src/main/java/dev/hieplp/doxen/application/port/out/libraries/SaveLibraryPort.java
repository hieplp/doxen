package dev.hieplp.doxen.application.port.out.libraries;

import dev.hieplp.doxen.domain.model.Library;

public interface SaveLibraryPort {
    Library save(Library library);
}
