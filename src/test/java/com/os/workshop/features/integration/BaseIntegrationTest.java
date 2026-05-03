package com.os.workshop.features.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that need a real PostgreSQL database.
 *
 * <p>Starts a PostgreSQL 16 container (matching docker-compose) once and reuses it
 * across all test classes that extend this. The datasource properties are injected
 * dynamically via {@link DynamicPropertySource}, overriding whatever is in application.yml.
 *
 * <p>Uses {@code create-drop} DDL strategy so Hibernate creates all tables from
 * entity mappings at startup and drops them at shutdown — validating that every
 * {@code @Entity}, {@code @Column}, {@code @Convert}, and relationship annotation
 * produces valid PostgreSQL DDL.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration")
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("workshop_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
