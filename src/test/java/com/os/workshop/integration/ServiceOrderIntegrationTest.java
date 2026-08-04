package com.os.workshop.integration;

import com.os.workshop.integration.config.IntegrationTestBase;
import com.os.workshop.infrastructure.persistence.service.ServiceTypeEntity;
import com.os.workshop.infrastructure.persistence.service.ServiceTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ServiceTypeJpaRepository serviceTypeJpaRepository;

    @BeforeEach
    void setupServiceTypes() {
        if (serviceTypeJpaRepository.findByName("TROCA_OLEO").isEmpty()) {
            var entity = new ServiceTypeEntity();
            entity.setName("TROCA_OLEO");
            entity.setDescription("Troca de óleo do motor");
            serviceTypeJpaRepository.save(entity);
        }
        if (serviceTypeJpaRepository.findByName("ALINHAMENTO").isEmpty()) {
            var entity = new ServiceTypeEntity();
            entity.setName("ALINHAMENTO");
            entity.setDescription("Alinhamento e balanceamento");
            serviceTypeJpaRepository.save(entity);
        }
    }

    @Test
    void should_create_service_order_with_full_flow_and_update_status() {
        var token = authenticateAsAdmin();

        // Step 1: Create client
        var clientId = createClient(token, "98765432100");

        // Step 2: Create vehicle for that client
        createVehicle(token, clientId, "XYZ9A88");

        // Step 3: Create service order (needs existing client CPF + vehicle plate + service types)
        var orderBody = Map.of(
                "cpfCnpj", "98765432100",
                "placaVeiculo", "XYZ9A88",
                "serviceTypes", List.of("TROCA_OLEO", "ALINHAMENTO")
        );
        var createResponse = restTemplate.exchange("/order", HttpMethod.POST,
                new HttpEntity<>(orderBody, authHeaders(token)), Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var orderId = (String) createResponse.getBody().get("id");
        assertThat(orderId).isNotBlank();
        assertThat(createResponse.getBody().get("serviceStatus")).isEqualTo("RECEBIDA");
        assertThat(createResponse.getBody().get("cpfCnpj")).isEqualTo("98765432100");
        assertThat(createResponse.getBody().get("placaVeiculo")).isEqualTo("XYZ9A88");

        // Step 4: Find order by ID
        var findResponse = restTemplate.exchange("/order/id/" + orderId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findResponse.getBody().get("id")).isEqualTo(orderId);

        // Step 5: List orders
        var listResponse = restTemplate.exchange("/order", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), List.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();

        // Step 6: Update order status to EM_DIAGNOSTICO
        var updateBody = Map.of("status", "EM_DIAGNOSTICO");
        var updateResponse = restTemplate.exchange("/order/" + orderId, HttpMethod.PATCH,
                new HttpEntity<>(updateBody, authHeaders(token)), Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify status changed by fetching the order
        var verifyResponse = restTemplate.exchange("/order/id/" + orderId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(verifyResponse.getBody().get("serviceStatus")).isEqualTo("EM_DIAGNOSTICO");

        // Step 7: Verify services were created for this OS
        var servicesResponse = restTemplate.exchange("/services/os/" + orderId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), List.class);
        assertThat(servicesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(servicesResponse.getBody()).hasSize(2);
    }
}
