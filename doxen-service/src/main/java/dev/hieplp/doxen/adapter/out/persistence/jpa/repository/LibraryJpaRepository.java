package dev.hieplp.doxen.adapter.out.persistence.jpa.repository;

import dev.hieplp.doxen.adapter.out.persistence.jpa.entity.LibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryJpaRepository extends JpaRepository<LibraryEntity, String> {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndLibraryIdNot(String slug, String libraryId);
}
