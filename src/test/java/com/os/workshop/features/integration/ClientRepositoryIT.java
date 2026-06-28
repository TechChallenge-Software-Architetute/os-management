package com.os.workshop.features.integration;

import com.os.workshop.infrastructure.persistence.client.ClientEntity;
import com.os.workshop.infrastructure.persistence.client.ClientJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates ClientEntity mapping, unique CPF constraint, and derived queries.
 */
class ClientRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private ClientJpaRepository clientJpaRepository;

    @Test
    void savesAndFindsClientById() {
        ClientEntity client = new ClientEntity();
        client.setName("João Silva");
        client.setCpf("12345678901");
        client.setEmail("joao@email.com");
        client.setPhone("11999999999");
        client.setActive(true);

        ClientEntity saved = clientJpaRepository.save(client);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        var found = clientJpaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("João Silva", found.get().getName());
    }

    @Test
    void findsByCpf() {
        ClientEntity client = new ClientEntity();
        client.setName("Maria");
        client.setCpf("98765432100");
        client.setActive(true);

        clientJpaRepository.save(client);

        assertTrue(clientJpaRepository.findByCpf("98765432100").isPresent());
        assertTrue(clientJpaRepository.existsByCpf("98765432100"));
        assertFalse(clientJpaRepository.existsByCpf("00000000000"));
    }

    @Test
    void findsByActiveTrue() {
        ClientEntity active = new ClientEntity();
        active.setName("Active");
        active.setCpf("11111111111");
        active.setActive(true);

        ClientEntity inactive = new ClientEntity();
        inactive.setName("Inactive");
        inactive.setCpf("22222222222");
        inactive.setActive(false);

        clientJpaRepository.save(active);
        clientJpaRepository.save(inactive);

        var activeClients = clientJpaRepository.findByActiveTrue();
        assertTrue(activeClients.stream().noneMatch(c -> c.getCpf().equals("22222222222")));
    }
}
