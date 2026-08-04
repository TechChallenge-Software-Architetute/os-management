package com.os.workshop.integration;

import com.os.workshop.integration.config.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIntegrationTest extends IntegrationTestBase {

    @Test
    void should_create_client_find_by_id_and_list() {
        var token = authenticateAsAdmin();

        // Create client
        var createBody = Map.of(
                "name", "Maria Silva",
                "document", "52998224725",
                "email", "maria@test.com",
                "phone", "11988887777"
        );
        var createResponse = restTemplate.exchange("/api/clients", HttpMethod.POST,
                new HttpEntity<>(createBody, authHeaders(token)), Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var clientId = ((Number) createResponse.getBody().get("id")).longValue();
        assertThat(clientId).isPositive();

        // Find by ID
        var findResponse = restTemplate.exchange("/api/clients/" + clientId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findResponse.getBody().get("name")).isEqualTo("MARIA SILVA");
        assertThat(findResponse.getBody().get("document")).isEqualTo("52998224725");
        assertThat(findResponse.getBody().get("email")).isEqualTo("maria@test.com");
        assertThat((Boolean) findResponse.getBody().get("active")).isTrue();

        // Find by CPF
        var cpfResponse = restTemplate.exchange("/api/clients/cpf/52998224725", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(cpfResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cpfResponse.getBody().get("id")).isEqualTo((int) clientId);

        // List all
        var listResponse = restTemplate.exchange("/api/clients", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), List.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }
}
