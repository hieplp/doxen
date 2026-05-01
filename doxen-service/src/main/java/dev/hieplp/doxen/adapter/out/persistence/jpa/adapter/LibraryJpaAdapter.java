package dev.hieplp.doxen.adapter.out.persistence.jpa.adapter;

import dev.hieplp.doxen.adapter.out.persistence.jpa.mapper.LibraryJpaMapper;
import dev.hieplp.doxen.adapter.out.persistence.jpa.repository.LibraryJpaRepository;
import dev.hieplp.doxen.application.port.out.libraries.ExistLibraryPort;
import dev.hieplp.doxen.application.port.out.libraries.FindLibraryPort;
import dev.hieplp.doxen.application.port.out.libraries.SaveLibraryPort;
import dev.hieplp.doxen.domain.model.Library;
import dev.hieplp.doxen.domain.vo.LibraryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LibraryJpaAdapter implements SaveLibraryPort, ExistLibraryPort, FindLibraryPort {

    private final LibraryJpaRepository repository;
    private final LibraryJpaMapper mapper;

    // SaveLibraryPort
    @Override
    public Library save(Library library) {
        return mapper.toDomain(repository.save(mapper.toEntity(library)));
    }

    // ExistLibraryPort
    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public boolean existsBySlugAndLibraryIdNot(String slug, LibraryId libraryId) {
        return repository.existsBySlugAndLibraryIdNot(slug, libraryId.value());
    }

    // FindLibraryPort
    @Override
    public Optional<Library> findById(LibraryId libraryId) {
        return repository.findById(libraryId.value()).map(mapper::toDomain);
    }
}
