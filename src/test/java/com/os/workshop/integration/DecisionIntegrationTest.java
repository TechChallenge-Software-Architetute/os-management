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

        var clientToken = createAndAuthenticateClient("client.approve@test.com", "45532895004");

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

        var clientId = createClient(adminToken, "71855sjp906");
        createVehicle(adminToken, clientId, "DEC2B02");

        var orderId = createServiceOrder(adminToken, "71855906906", "DEC2B02");

        advanceStatus(adminToken, orderId, "EM_DIAGNOSTICO");
        advanceStatus(adminToken, orderId, "AGUARDANDO_APROVACAO");

        var clientToken = createAndAuthenticateClient("client.reject@test.com", "71855906906");

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
        var otherToken = createAndAuthenticateClient("other.client@test.com", "19131243004");

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

        var clientToken = createAndAuthenticateClient("client.invalid@test.com", "30455923059");

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

    private String createAndAuthenticateClient(String email, String document) {
        var adminToken = authenticateAsAdmin();

        var signupBody = Map.of("email", email, "password", "Test123456", "roles", List.of("USER"));
        restTemplate.exchange("/signup", HttpMethod.POST,
                new HttpEntity<>(signupBody, authHeaders(adminToken)), Map.class);

        var loginBody = Map.of("email", email, "password", "Test123456");
        var loginResponse = restTemplate.exchange("/auth/login", HttpMethod.POST,
                new HttpEntity<>(loginBody, jsonHeaders()), Map.class);
        return (String) loginResponse.getBody().get("token");
    }

    private HttpHeaders jsonHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
