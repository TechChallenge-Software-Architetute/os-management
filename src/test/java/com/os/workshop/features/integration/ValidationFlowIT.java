package com.os.workshop.features.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class ValidationFlowIT {

    private static final String SUPERADMIN_EMAIL = "superadmin@system.com";
    private static final String SUPERADMIN_PASSWORD = "coxinha123";
    private static final String JOAO_EMAIL = "joao.silva@email.com";
    private static final String JOAO_PASSWORD = "Coxinha321";
    private static final String JOAO_CPF = "52998224725";

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("workshop_validation_test")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forListeningPort());

    static {
        postgres.start();
    }

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        cleanDatabase();
        seedValidationData();
    }

    @Test
    void shouldExecuteValidationScriptFlowWithRestAssured() throws Exception {
        String token = login(SUPERADMIN_EMAIL, SUPERADMIN_PASSWORD);

        String orderId = given()
                .contentType("application/json")
                .header("Authorization", bearer(token))
                .header("correlationid", "d29797dd-0eca-4ee0-918d-466ed0c8886e")
                .body(Map.of(
                        "cpfCnpj", JOAO_CPF,
                        "placaVeiculo", "ABC-1234",
                        "serviceTypes", List.of("TROCA_OLEO", "ALINHAMENTO")
                ))
                .when()
                .post("/order")
                .then()
                .statusCode(201)
                .body("id", not(blankOrNullString()))
                .body("serviceStatus", equalTo("RECEBIDA"))
                .extract()
                .path("id");

        updateOrderStatus(token, orderId, "EM_DIAGNOSTICO")
                .body("serviceStatus", equalTo("EM_DIAGNOSTICO"));

        given()
                .header("Authorization", bearer(token))
                .when()
                .get("/order/{orderId}", orderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId))
                .body("serviceStatus", equalTo("EM_DIAGNOSTICO"));

        given()
                .header("Authorization", bearer(token))
                .when()
                .get("/api/parts")
                .then()
                .statusCode(200)
                .body("id", hasItem(1));

        given()
                .header("Authorization", bearer(token))
                .when()
                .get("/api/stocks")
                .then()
                .statusCode(200)
                .body("productId", hasItem(1));

        given()
                .contentType("application/json")
                .header("Authorization", bearer(token))
                .body(Map.of(
                        "serviceOrderId", orderId,
                        "items", List.of(Map.of(
                                "productId", 1,
                                "quantity", new BigDecimal("3.5")
                        ))
                ))
                .when()
                .post("/api/stocks/reservations")
                .then()
                .statusCode(201)
                .body("serviceOrderId", hasItem(orderId))
                .body("productId", hasItem(1));

        updateOrderStatus(token, orderId, "AGUARDANDO_APROVACAO")
                .body("serviceStatus", equalTo("AGUARDANDO_APROVACAO"));

        given()
                .header("Authorization", bearer(token))
                .when()
                .get("/api/budgets/service-order/{orderId}", orderId)
                .then()
                .statusCode(200)
                .body("serviceOrderId", equalTo(orderId))
                .body("items", hasSize(1))
                .body("totalPrice", comparesEqualTo(314.65F));

        given()
                .contentType("application/json")
                .header("Authorization", bearer(token))
                .body(Map.of(
                        "email", JOAO_EMAIL,
                        "password", JOAO_PASSWORD,
                        "roles", List.of("USER")
                ))
                .when()
                .post("/signup")
                .then()
                .statusCode(200)
                .body("email", equalTo(JOAO_EMAIL))
                .body("roles", hasItem("ROLE_USER"));

        String joaoToken = login(JOAO_EMAIL, JOAO_PASSWORD);

        given()
                .header("Authorization", bearer(joaoToken))
                .when()
                .get("/api/clients/my-orders")
                .then()
                .statusCode(200)
                .body("orderId", hasItem(orderId))
                .body("serviceStatus", hasItem("AGUARDANDO_APROVACAO"));

        given()
                .header("Authorization", bearer(joaoToken))
                .when()
                .get("/api/clients/my-orders/{orderId}", orderId)
                .then()
                .statusCode(200)
                .body("orderId", equalTo(orderId))
                .body("serviceStatus", equalTo("AGUARDANDO_APROVACAO"))
                .body("budget.serviceOrderId", equalTo(orderId));

        given()
                .header("Authorization", bearer(joaoToken))
                .when()
                .patch("/api/clients/my-orders/{orderId}/approve", orderId)
                .then()
                .statusCode(200)
                .body("orderId", equalTo(orderId))
                .body("serviceStatus", equalTo("APROVADO"));

        List<String> serviceIds = given()
                .header("Authorization", bearer(token))
                .when()
                .get("/services/os/{orderId}", orderId)
                .then()
                .statusCode(200)
                .body("id", hasSize(2))
                .body("serviceTypeName", containsInAnyOrder("TROCA_OLEO", "ALINHAMENTO"))
                .extract()
                .path("id");

        updateServiceStatus(token, serviceIds.get(0), "DOING");
        Thread.sleep(10);
        updateServiceStatus(token, serviceIds.get(0), "DONE");
        updateServiceStatus(token, serviceIds.get(1), "DOING");
        Thread.sleep(10);
        updateServiceStatus(token, serviceIds.get(1), "DONE");

        updateOrderStatus(token, orderId, "FINALIZADA")
                .body("serviceStatus", equalTo("FINALIZADA"));

        updateOrderStatus(token, orderId, "ENTREGUE")
                .body("serviceStatus", equalTo("ENTREGUE"));

        given()
                .contentType("application/json")
                .header("Authorization", bearer(token))
                .body(Map.of("timeUnit", "SECONDS"))
                .when()
                .post("/monitoring/all")
                .then()
                .statusCode(200)
                .body("serviceTypeName", hasItems("TROCA_OLEO", "ALINHAMENTO"));
    }

    private String login(String email, String password) {
        return given()
                .contentType("application/json")
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", not(blankOrNullString()))
                .extract()
                .path("token");
    }

    private io.restassured.response.ValidatableResponse updateOrderStatus(String token, String orderId, String status) {
        return given()
                .contentType("application/json")
                .header("Authorization", bearer(token))
                .body(Map.of("status", status))
                .when()
                .patch("/order/{orderId}", orderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId));
    }

    private void updateServiceStatus(String token, String serviceId, String status) {
        given()
                .contentType("application/json")
                .header("Authorization", bearer(token))
                .body(Map.of("status", status, "id", serviceId))
                .when()
                .patch("/services/update-status")
                .then()
                .statusCode(200)
                .body("id", equalTo(serviceId));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanDatabase() {
        jdbcTemplate.update("delete from budget_items");
        jdbcTemplate.update("delete from budgets");
        jdbcTemplate.update("delete from stock_reservations");
        jdbcTemplate.update("delete from stock_movements");
        jdbcTemplate.update("delete from stocks");
        jdbcTemplate.update("delete from service");
        jdbcTemplate.update("delete from service_order");
        jdbcTemplate.update("delete from parts");
        jdbcTemplate.update("delete from products");
        jdbcTemplate.update("delete from vehicles");
        jdbcTemplate.update("delete from clients");
        jdbcTemplate.update("delete from user_groups");
        jdbcTemplate.update("delete from user_roles");
        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("delete from roles");
        jdbcTemplate.update("delete from service_type");
    }

    private void seedValidationData() {
        UUID adminId = UUID.randomUUID();
        UUID roleAdminId = UUID.randomUUID();
        UUID roleUserId = UUID.randomUUID();
        UUID roleTechnicianId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update("insert into roles (id, name) values (?, ?)", roleAdminId, "ROLE_ADMIN");
        jdbcTemplate.update("insert into roles (id, name) values (?, ?)", roleUserId, "ROLE_USER");
        jdbcTemplate.update("insert into roles (id, name) values (?, ?)", roleTechnicianId, "ROLE_TECHNICIAN");
        jdbcTemplate.update(
                "insert into users (id, email, password) values (?, ?, ?)",
                adminId,
                SUPERADMIN_EMAIL,
                passwordEncoder.encode(SUPERADMIN_PASSWORD)
        );
        jdbcTemplate.update("insert into user_roles (user_id, role_id) values (?, ?)", adminId, roleAdminId);
        jdbcTemplate.update("insert into user_roles (user_id, role_id) values (?, ?)", adminId, roleUserId);
        jdbcTemplate.update("insert into user_roles (user_id, role_id) values (?, ?)", adminId, roleTechnicianId);

        jdbcTemplate.update(
                """
                insert into clients (id, name, cpf, email, phone, active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                "JOAO DA SILVA",
                JOAO_CPF,
                JOAO_EMAIL,
                "(11) 99999-1234",
                true,
                now,
                now
        );
        jdbcTemplate.update(
                """
                insert into vehicles (id, client_id, plate, brand, model, year, color, type, active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                1L,
                "ABC1234",
                "TOYOTA",
                "COROLLA",
                2020,
                "PRATA",
                "CAR",
                true,
                now,
                now
        );

        jdbcTemplate.update(
                "insert into service_type (id, name, description) values (?, ?, ?)",
                UUID.randomUUID(),
                "TROCA_OLEO",
                "Substituicao do oleo do motor e filtro"
        );
        jdbcTemplate.update(
                "insert into service_type (id, name, description) values (?, ?, ?)",
                UUID.randomUUID(),
                "ALINHAMENTO",
                "Ajuste da geometria das rodas"
        );

        jdbcTemplate.update(
                """
                insert into products (
                    id, product_type, name, sku, unit, category, brand, cost_price, sale_price, active, created_at, updated_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                "PART",
                "Pastilha de Freio Dianteira",
                "BRK-PAD-001",
                "UNIT",
                "Freios",
                "Bosch",
                new BigDecimal("45.00"),
                new BigDecimal("89.90"),
                true,
                now,
                now
        );
        jdbcTemplate.update(
                "insert into parts (id, manufacturer_code, warranty_months) values (?, ?, ?)",
                1L,
                "BOH-BP-2025",
                12
        );
        jdbcTemplate.update(
                """
                insert into stocks (id, product_id, quantity, reserved_quantity, minimum_quantity, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                1L,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                now,
                now
        );
    }
}
