package com.os.features.client.persistence.repository;

import com.os.features.client.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para {@link ClientEntity}.
 *
 * <p>Usado exclusivamente pelo {@code ClientPersistenceAdapter} — não deve ser injetado
 * diretamente em serviços de aplicação ou domínio, mantendo o isolamento de camadas.
 */
@Repository
public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {

    /**
     * Busca um cliente pelo CPF normalizado (sem formatação).
     *
     * @param cpf CPF com 11 dígitos, sem pontuação
     * @return {@code Optional} com a entidade ou vazio
     */
    Optional<ClientEntity> findByCpf(String cpf);

    /**
     * Retorna todos os clientes marcados como ativos.
     *
     * @return lista de entidades com {@code active = true}
     */
    List<ClientEntity> findByActiveTrue();

    /**
     * Verifica se existe algum cliente com o CPF informado.
     * Mais eficiente que {@code findByCpf} quando apenas a existência importa.
     *
     * @param cpf CPF com 11 dígitos, sem pontuação
     * @return {@code true} se o CPF já estiver cadastrado
     */
    boolean existsByCpf(String cpf);
}
