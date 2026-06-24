package com.os.workshop.features.client.shared.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para {@link ClientEntity}.
 *
 * <p>Usado exclusivamente pelo {@code ClientPersistenceAdapter} — não deve ser injetado
 * diretamente em serviços de aplicação ou domínio, mantendo o isolamento de camadas.
 */
@Repository
public interface ClientJpaRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByCpf(String cpf);

    List<ClientEntity> findByActiveTrue();

    boolean existsByCpf(String cpf);

    Optional<ClientEntity> findByEmail(String email);
}
