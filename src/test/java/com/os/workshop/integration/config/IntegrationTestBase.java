package com.os.workshop.integration.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Base class for all integration tests.
 * Provides TestRestTemplate, authentication helpers, and entity creation helpers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Import(TestcontainersConfig.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    private static final String ADMIN_EMAIL = "admin-test@workshop.com";
    private static final String ADMIN_PASSWORD = "Admin@123";

    /** Must match jwt.secret in src/test/resources/application-integration.yml. */
    private static final String INTEGRATION_JWT_SECRET =
            "test-secret-key-for-integration-tests-only-min-32-chars";

    // ==================== Auth Helpers ====================

    protected String authenticateAsAdmin() {
        signUp(ADMIN_EMAIL, ADMIN_PASSWORD, Set.of("ADMIN"));
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    /**
     * Mints a JWT identical in shape to the one issued by the serverless CPF auth function
     * (os-management-lambda): subject = CPF/CNPJ digits, a {@code clientId} claim, role CLIENT.
     * Lets integration tests exercise the client portal without deploying the Lambda.
     */
    protected String clientToken(String document, Long clientId) {
        var key = Keys.hmacShaKeyFor(INTEGRATION_JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(document.replaceAll("\\D", ""))
                .claim("clientId", clientId)
                .claim("name", "Cliente Teste")
                .claim("roles", List.of("CLIENT"))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 3_600_000L))
                .signWith(key)
                .compact();
    }

    protected String authenticateAsUser(String email, String password) {
        signUp(email, password, Set.of("USER"));
        return login(email, password);
    }

    protected void signUp(String email, String password, Set<String> roles) {
        var body = Map.of("email", email, "password", password, "roles", roles);
        restTemplate.postForEntity("/signup", body, Object.class);
    }

    protected String login(String email, String password) {
        var body = Map.of("email", email, "password", password);
        var response = restTemplate.postForEntity("/auth/login", body, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("token");
        }
        throw new RuntimeException("Failed to login: " + response.getStatusCode());
    }

    protected HttpHeaders authHeaders(String token) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ==================== Entity Creation Helpers ====================

    protected Long createClient(String token, String cpf) {
        var body = Map.of(
                "name", "Cliente Teste",
                "document", cpf,
                "email", "cliente-" + cpf + "@test.com",
                "phone", "11999999999"
        );
        var response = restTemplate.exchange("/api/clients", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(token)), Map.class);
        assertCreated(response);
        return ((Number) response.getBody().get("id")).longValue();
    }

    protected Long createVehicle(String token, Long clientId, String plate) {
        var body = Map.of(
                "clientId", clientId,
                "plate", plate,
                "brand", "Toyota",
                "model", "Corolla",
                "year", 2023,
                "color", "Preto",
                "type", "CAR"
        );
        var response = restTemplate.exchange("/api/vehicles", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(token)), Map.class);
        assertCreated(response);
        return ((Number) response.getBody().get("id")).longValue();
    }

    protected Long createPart(String token, String sku) {
        var body = Map.ofEntries(
                Map.entry("name", "Pastilha de Freio"),
                Map.entry("sku", sku),
                Map.entry("unit", "UNIT"),
                Map.entry("category", "Freios"),
                Map.entry("brand", "Bosch"),
                Map.entry("costPrice", 45.00),
                Map.entry("salePrice", 89.90),
                Map.entry("manufacturerCode", "BOH-BP-2025"),
                Map.entry("warrantyMonths", 12)
        );
        var response = restTemplate.exchange("/api/parts", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(token)), Map.class);
        assertCreated(response);
        return ((Number) response.getBody().get("id")).longValue();
    }

    protected Long createSupply(String token, String sku) {
        var body = Map.ofEntries(
                Map.entry("name", "Óleo Motor 5W30"),
                Map.entry("sku", sku),
                Map.entry("unit", "LITER"),
                Map.entry("category", "Lubrificantes"),
                Map.entry("brand", "Mobil"),
                Map.entry("costPrice", 25.00),
                Map.entry("salePrice", 49.90),
                Map.entry("fractionalAllowed", true),
                Map.entry("packageSize", 1.0)
        );
        var response = restTemplate.exchange("/api/supplies", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(token)), Map.class);
        assertCreated(response);
        return ((Number) response.getBody().get("id")).longValue();
    }

    protected Long createStock(String token, Long productId, BigDecimal quantity, BigDecimal minimumQuantity) {
        var body = Map.of(
                "productId", productId,
                "quantity", quantity,
                "minimumQuantity", minimumQuantity
        );
        var response = restTemplate.exchange("/api/stocks", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(token)), Map.class);
        assertCreated(response);
        return ((Number) response.getBody().get("id")).longValue();
    }

    // ==================== Assertion Helpers ====================

    protected void assertCreated(ResponseEntity<?> response) {
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new AssertionError("Expected 201 CREATED but got " + response.getStatusCode()
                    + " body: " + response.getBody());
        }
    }
}
