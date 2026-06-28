package com.os.workshop.application.client.port.out;

import com.os.workshop.domain.client.Client;

import java.util.List;
import java.util.Optional;

/**
 * Output port para operações de persistência de Client.
 */
public interface ClientRepository {

    Client save(Client client);

    Optional<Client> findById(Long id);

    Optional<Client> findByCpf(String normalizedCpf);

    List<Client> findAllActive();

    boolean existsByCpf(String normalizedCpf);

    Optional<Client> findByEmail(String email);
}
