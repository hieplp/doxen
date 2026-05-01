package dev.hieplp.doxen.application.port.in.libraries;

public interface UpdateLibraryUseCase {

    UpdateLibraryResult update(UpdateLibraryCommand command);

    sealed interface UpdateLibraryResult {
        record Success(String libraryId) implements UpdateLibraryResult {
        }

        record DuplicateSlug() implements UpdateLibraryResult {
        }
    }

    record UpdateLibraryCommand(
            String libraryId,
            String name,
            String description,
            String homepageUrl
    ) {
    }
}
