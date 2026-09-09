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

class DecisionIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ServiceTypeJpaRepository serviceTypeJpaRepository;

    @BeforeEach
    void setupServiceTypes() {
        if (serviceTypeJpaRepository.findByName("TROCA_OLEO").isEmpty()) {
            var entity = new ServiceTypeEntity();
            entity.setName("TROCA_OLEO");
            entity.setDescription("Troca de oleo");
            serviceTypeJpaRepository.save(entity);
        }
    }

    @Test
    void should_approve_order_via_decision_endpoint() {
        var adminToken = authenticateAsAdmin();

        var clientId = createClient(adminToken, "45532895004");
        createVehicle(adminToken, clientId, "DEC1A01");

        var orderId = createServiceOrder(adminToken, "45532895004", "DEC1A01");

        advanceStatus(adminToken, orderId, "EM_DIAGNOSTICO");
        advanceStatus(adminToken, orderId, "AGUARDANDO_APROVACAO");

        var clientToken = clientToken("45532895004", clientId);

        var decisionBody = Map.of("decision", "APPROVED");
        var response = restTemplate.exchange(
                "/api/clients/my-orders/" + orderId + "/decision",
                HttpMethod.POST,
                new HttpEntity<>(decisionBody, authHeaders(clientToken)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var orderResponse = restTemplate.exchange(
                "/order/id/" + orderId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(adminToken)), Map.class);
        assertThat(orderResponse.getBody().get("serviceStatus")).isEqualTo("APROVADO");
    }

    @Test
    void should_reject_order_and_release_reservations() {
        var adminToken = authenticateAsAdmin();

        var clientId = createClient(adminToken, "71855906906");
        createVehicle(adminToken, clientId, "DEC2B02");

        var orderId = createServiceOrder(adminToken, "71855906906", "DEC2B02");

        advanceStatus(adminToken, orderId, "EM_DIAGNOSTICO");
        advanceStatus(adminToken, orderId, "AGUARDANDO_APROVACAO");

        var clientToken = clientToken("71855906906", clientId);

        var decisionBody = Map.of("decision", "REJECTED", "reason", "Muito caro");
        var response = restTemplate.exchange(
                "/api/clients/my-orders/" + orderId + "/decision",
                HttpMethod.POST,
                new HttpEntity<>(decisionBody, authHeaders(clientToken)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var orderResponse = restTemplate.exchange(
                "/order/id/" + orderId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(adminToken)), Map.class);
        assertThat(orderResponse.getBody().get("serviceStatus")).isEqualTo("RECUSADA");
        assertThat(orderResponse.getBody().get("rejectionReason")).isEqualTo("Muito caro");
    }

    @Test
    void should_return_403_when_not_owner() {
        var adminToken = authenticateAsAdmin();

        var clientId = createClient(adminToken, "82178638070");
        createVehicle(adminToken, clientId, "DEC3C03");

        var orderId = createServiceOrder(adminToken, "82178638070", "DEC3C03");

        advanceStatus(adminToken, orderId, "EM_DIAGNOSTICO");
        advanceStatus(adminToken, orderId, "AGUARDANDO_APROVACAO");

        var otherClientId = createClient(adminToken, "19131243004");
        var otherToken = clientToken("19131243004", otherClientId);

        var decisionBody = Map.of("decision", "APPROVED");
        var response = restTemplate.exchange(
                "/api/clients/my-orders/" + orderId + "/decision",
                HttpMethod.POST,
                new HttpEntity<>(decisionBody, authHeaders(otherToken)),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_return_422_when_invalid_transition() {
        var adminToken = authenticateAsAdmin();

        var clientId = createClient(adminToken, "30455923059");
        createVehicle(adminToken, clientId, "DEC4D04");

        var orderId = createServiceOrder(adminToken, "30455923059", "DEC4D04");

        var clientToken = clientToken("30455923059", clientId);

        var decisionBody = Map.of("decision", "APPROVED");
        var response = restTemplate.exchange(
                "/api/clients/my-orders/" + orderId + "/decision",
                HttpMethod.POST,
                new HttpEntity<>(decisionBody, authHeaders(clientToken)),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void should_find_orders_by_document() {
        var adminToken = authenticateAsAdmin();

        var clientId = createClient(adminToken, "65010578097");
        createVehicle(adminToken, clientId, "DEC5E05");
        createServiceOrder(adminToken, "65010578097", "DEC5E05");
        createServiceOrder(adminToken, "65010578097", "DEC5E05");

        var response = restTemplate.exchange(
                "/order/document/65010578097", HttpMethod.GET,
                new HttpEntity<>(authHeaders(adminToken)), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }

    private String createServiceOrder(String token, String cpf, String plate) {
        var orderBody = Map.of(
                "cpfCnpj", cpf,
                "placaVeiculo", plate,
                "serviceTypes", List.of("TROCA_OLEO"));
        var response = restTemplate.exchange("/order", HttpMethod.POST,
                new HttpEntity<>(orderBody, authHeaders(token)), Map.class);
        return (String) response.getBody().get("id");
    }

    private void advanceStatus(String token, String orderId, String status) {
        var body = Map.of("status", status);
        restTemplate.exchange("/order/" + orderId, HttpMethod.PATCH,
                new HttpEntity<>(body, authHeaders(token)), Void.class);
    }
}
