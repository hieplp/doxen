package dev.hieplp.doxen.application.service.libraries;

import dev.hieplp.doxen.application.port.in.libraries.CreateLibraryUseCase;
import dev.hieplp.doxen.application.port.out.libraries.ExistLibraryPort;
import dev.hieplp.doxen.application.port.out.libraries.SaveLibraryPort;
import dev.hieplp.doxen.common.util.SlugUtil;
import dev.hieplp.doxen.domain.enums.LibraryStatus;
import dev.hieplp.doxen.domain.model.Library;
import dev.hieplp.doxen.domain.vo.LibraryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateLibraryService implements CreateLibraryUseCase {

    private final ExistLibraryPort existLibraryPort;
    private final SaveLibraryPort saveLibraryPort;

    @Override
    public CreateLibraryResult create(CreateLibraryCommand command) {
        log.info("Creating library. name={}", command.name());

        final var slug = SlugUtil.toSlug(command.name());

        if (existLibraryPort.existsBySlug(slug)) {
            log.warn("Library slug already exists. slug={}", slug);
            return new CreateLibraryResult.DuplicateSlug();
        }

        final var libraryId = new LibraryId(UUID.randomUUID().toString());
        saveLibraryPort.save(Library.builder()
                .libraryId(libraryId)
                .slug(slug)
                .name(command.name())
                .description(command.description())
                .homepageUrl(command.homepageUrl())
                .status(LibraryStatus.ACTIVE)
                .build());

        log.info("Created library successfully. libraryId={}", libraryId);
        return new CreateLibraryResult.Success(libraryId.value());
    }

}
