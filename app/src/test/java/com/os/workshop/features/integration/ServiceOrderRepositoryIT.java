package com.os.workshop.features.integration;

import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the ServiceOrderEntity mapping, including the ListToJsonConverter
 * that serializes List<String> to a JSON column in PostgreSQL.
 */
class ServiceOrderRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Test
    void savesOrderWithJsonListConverter() {
        UUID id = UUID.randomUUID();

        ServiceOrderEntity order = new ServiceOrderEntity();
        order.setId(id);
        order.setServiceTypeName("[TROCA_OLEO, ALINHAMENTO]");
        order.setServiceStatus("RECEBIDA");
        order.setListService(List.of("TROCA_OLEO", "ALINHAMENTO"));
        order.setCpfCnpj("12345678901");
        order.setPlacaVeiculo("ABC1234");

        serviceOrderJpaRepository.save(order);

        var found = serviceOrderJpaRepository.findById(id);
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getListService().size());
        assertTrue(found.get().getListService().contains("TROCA_OLEO"));
        assertTrue(found.get().getListService().contains("ALINHAMENTO"));
        assertEquals("RECEBIDA", found.get().getServiceStatus());
    }

    @Test
    void findsByCpfCnpj() {
        UUID id = UUID.randomUUID();

        ServiceOrderEntity order = new ServiceOrderEntity();
        order.setId(id);
        order.setServiceTypeName("[REVISAO]");
        order.setServiceStatus("RECEBIDA");
        order.setListService(List.of("REVISAO"));
        order.setCpfCnpj("99988877766");
        order.setPlacaVeiculo("XYZ9876");

        serviceOrderJpaRepository.save(order);

        var results = serviceOrderJpaRepository.findByCpfCnpj("99988877766");
        assertEquals(1, results.size());
    }
}
