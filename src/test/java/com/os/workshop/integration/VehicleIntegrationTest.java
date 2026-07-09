package com.os.workshop.integration;

import com.os.workshop.integration.config.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleIntegrationTest extends IntegrationTestBase {

    @Test
    void should_create_vehicle_and_find_by_plate() {
        var token = authenticateAsAdmin();

        // Pre-requisite: create client
        var clientId = createClient(token, "11122233344");

        // Create vehicle
        var vehicleBody = Map.of(
                "clientId", clientId,
                "plate", "ABC1D23",
                "brand", "Honda",
                "model", "Civic",
                "year", 2022,
                "color", "Branco",
                "type", "CAR"
        );
        var createResponse = restTemplate.exchange("/api/vehicles", HttpMethod.POST,
                new HttpEntity<>(vehicleBody, authHeaders(token)), Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var vehicleId = ((Number) createResponse.getBody().get("id")).longValue();
        assertThat(vehicleId).isPositive();
        assertThat(createResponse.getBody().get("clientId")).isEqualTo(clientId.intValue());

        // Find by plate
        var plateResponse = restTemplate.exchange("/api/vehicles/plate/ABC1D23", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(plateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(plateResponse.getBody().get("brand")).isEqualTo("Honda");
        assertThat(plateResponse.getBody().get("model")).isEqualTo("Civic");
        assertThat(plateResponse.getBody().get("type")).isEqualTo("CAR");

        // Find by client
        var clientVehiclesResponse = restTemplate.exchange("/api/vehicles/client/" + clientId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), List.class);
        assertThat(clientVehiclesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(clientVehiclesResponse.getBody()).hasSize(1);
    }
}
