package dev.hieplp.doxen.adapter.out.persistence.jpa.adapter;

import dev.hieplp.doxen.adapter.out.persistence.jpa.mapper.RootAccountJpaMapper;
import dev.hieplp.doxen.adapter.out.persistence.jpa.repository.RootAccountJpaRepository;
import dev.hieplp.doxen.application.port.out.root.account.ExistRootAccountPort;
import dev.hieplp.doxen.application.port.out.root.account.LoadRootAccountPort;
import dev.hieplp.doxen.application.port.out.root.account.SaveRootAccountPort;
import dev.hieplp.doxen.domain.model.RootAccount;
import dev.hieplp.doxen.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RootAccountAccountJpaAdapter implements ExistRootAccountPort, SaveRootAccountPort, LoadRootAccountPort {

    private final RootAccountJpaRepository repository;
    private final RootAccountJpaMapper mapper;

    @Override
    public boolean existsById(UserId userId) {
        return repository.existsById(userId.value());
    }

    @Override
    public RootAccount save(RootAccount rootAccount) {
        return mapper.toDomain(repository.save(mapper.toEntity(rootAccount)));
    }

    @Override
    public Optional<RootAccount> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDomain);
    }
}
