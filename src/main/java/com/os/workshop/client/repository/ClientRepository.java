package com.os.workshop.client.repository;

import com.os.workshop.client.domain.Client;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída (output port) para operações de persistência de {@link Client}.
 *
 * <p>Segue o padrão de inversão de dependência do DDD/Hexagonal: o domínio define a interface
 * e a camada de infraestrutura ({@code ClientPersistenceAdapter}) provê a implementação.
 * Isso mantém o domínio desacoplado de frameworks como Spring Data JPA.
 */
@Repository
public interface ClientRepository {

    /**
     * Persiste um cliente (insert ou update conforme presença de ID).
     *
     * @param client cliente a persistir
     * @return cliente com ID e timestamps preenchidos
     */
    Client save(Client client);

    /**
     * Busca um cliente pelo seu identificador único (Long).
     *
     * @param id identificador do cliente
     * @return {@code Optional} com o cliente ou vazio se não existir
     */
    Optional<Client> findById(Long id);

    /**
     * Busca um cliente pelo CPF normalizado (apenas dígitos, 11 caracteres).
     *
     * @param normalizedCpf CPF sem pontuação
     * @return {@code Optional} com o cliente ou vazio se não existir
     */
    Optional<Client> findByCpf(String normalizedCpf);

    /**
     * Retorna todos os clientes ativos.
     *
     * @return lista de clientes com {@code active = true}
     */
    List<Client> findAllActive();

    /**
     * Verifica se já existe um cliente com o CPF informado.
     *
     * @param normalizedCpf CPF sem pontuação
     * @return {@code true} se o CPF já estiver cadastrado
     */
    boolean existsByCpf(String normalizedCpf);
}
