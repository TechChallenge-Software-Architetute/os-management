package com.os.workshop.adapter.out.persistence.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.infrastructure.persistence.client.ClientEntity;
import com.os.workshop.infrastructure.persistence.client.ClientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientPersistenceAdapter implements ClientRepository {

    private final ClientJpaRepository jpaRepository;
    private final ClientMapper mapper;

    @Override
    public Client save(Client client) {
        ClientEntity entity;
        if (client.getId() != null) {
            entity = jpaRepository.findById(client.getId())
                    .orElse(mapper.toEntity(client));
            mapper.updateEntity(entity, client);
        } else {
            entity = mapper.toEntity(client);
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Client> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Client> findByDocument(String normalizedDocument) {
        return jpaRepository.findByDocument(normalizedDocument).map(mapper::toDomain);
    }

    @Override
    public List<Client> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByDocument(String normalizedDocument) {
        return jpaRepository.existsByDocument(normalizedDocument);
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }
}
