package dev.hieplp.doxen.adapter.out.persistence.jpa.repository;

import dev.hieplp.doxen.adapter.out.persistence.jpa.entity.RootAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RootAccountJpaRepository extends JpaRepository<RootAccountEntity, String> {
    Optional<RootAccountEntity> findByUsername(String username);
}
