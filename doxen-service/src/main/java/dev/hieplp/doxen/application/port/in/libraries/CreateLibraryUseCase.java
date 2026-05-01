package dev.hieplp.doxen.application.port.in.libraries;

public interface CreateLibraryUseCase {

    CreateLibraryResult create(CreateLibraryCommand command);

    sealed interface CreateLibraryResult {
        record Success(String libraryId) implements CreateLibraryResult {
        }

        record DuplicateSlug() implements CreateLibraryResult {
        }
    }

    record CreateLibraryCommand(
            String name,
            String description,
            String homepageUrl
    ) {
    }
}
