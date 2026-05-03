package com.os.workshop.features.client.persistence.adapter;

import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.persistence.entity.ClientEntity;
import com.os.workshop.features.client.persistence.mapper.ClientMapper;
import com.os.workshop.features.client.persistence.repository.ClientJpaRepository;
import com.os.workshop.features.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa a porta {@link ClientRepository} usando Spring Data JPA.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Decidir entre insert e update com base na presença de ID no aggregate.</li>
 *   <li>Converter entre o domínio ({@link Client}) e a entidade JPA ({@link ClientEntity})
 *       através do {@link ClientMapper}.</li>
 *   <li>Isolar o serviço de aplicação de qualquer detalhe de persistência.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class ClientPersistenceAdapter implements ClientRepository {

    @Autowired
    private final ClientJpaRepository jpaRepository;

    @Autowired
    private final ClientMapper mapper;

    /**
     * Persiste o cliente. Se o aggregate já possui ID, carrega a entidade existente e aplica
     * o update; caso contrário, cria uma nova entidade para insert.
     */
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
    public Optional<Client> findByCpf(String normalizedCpf) {
        return jpaRepository.findByCpf(normalizedCpf).map(mapper::toDomain);
    }

    @Override
    public List<Client> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCpf(String normalizedCpf) {
        return jpaRepository.existsByCpf(normalizedCpf);
    }
}
