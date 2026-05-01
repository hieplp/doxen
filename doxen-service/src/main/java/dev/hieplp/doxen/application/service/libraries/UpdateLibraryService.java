package dev.hieplp.doxen.application.service.libraries;

import dev.hieplp.doxen.application.port.in.libraries.UpdateLibraryUseCase;
import dev.hieplp.doxen.application.port.out.libraries.ExistLibraryPort;
import dev.hieplp.doxen.application.port.out.libraries.FindLibraryPort;
import dev.hieplp.doxen.application.port.out.libraries.SaveLibraryPort;
import dev.hieplp.doxen.common.util.SlugUtil;
import dev.hieplp.doxen.domain.vo.LibraryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateLibraryService implements UpdateLibraryUseCase {

    private final FindLibraryPort findLibraryPort;
    private final ExistLibraryPort existLibraryPort;
    private final SaveLibraryPort saveLibraryPort;

    @Override
    public UpdateLibraryResult update(UpdateLibraryCommand command) {
        log.info("Updating library. libraryId={}", command.libraryId());

        final var libraryId = new LibraryId(command.libraryId());
        final var existing = findLibraryPort.getById(libraryId);

        final var slug = SlugUtil.toSlug(command.name());
        if (existLibraryPort.existsBySlugAndLibraryIdNot(slug, libraryId)) {
            log.warn("Library slug already exists. slug={}", slug);
            return new UpdateLibraryResult.DuplicateSlug();
        }

        saveLibraryPort.save(existing.toBuilder()
                .libraryId(libraryId)
                .slug(slug)
                .name(command.name())
                .description(command.description())
                .homepageUrl(command.homepageUrl())
                .build());

        log.info("Updated library successfully. libraryId={}", libraryId);
        return new UpdateLibraryResult.Success(libraryId.value());
    }

}
